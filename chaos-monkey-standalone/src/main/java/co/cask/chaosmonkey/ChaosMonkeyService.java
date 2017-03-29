/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.chaosmonkey;

import co.cask.chaosmonkey.common.Constants;
import co.cask.chaosmonkey.common.conf.Configuration;
import co.cask.chaosmonkey.proto.Action;
import co.cask.chaosmonkey.proto.ActionArguments;
import co.cask.chaosmonkey.proto.ActionStatus;
import co.cask.chaosmonkey.proto.ClusterDisrupter;
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import co.cask.chaosmonkey.proto.ClusterNode;
import co.cask.chaosmonkey.proto.NodeStatus;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AbstractIdleService;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * {@link ChaosMonkeyService} Allows for user to perform disruptions directly
 */
public class ChaosMonkeyService extends AbstractIdleService implements ClusterDisrupter {
  private static final Logger LOG = LoggerFactory.getLogger(ChaosMonkeyService.class);

  private DisruptionService disruptionService;
  private final Table<String, String, RemoteProcess> processTable;
  private ExecutorService executor;
  private final Configuration conf;
  private final ClusterInfoCollector clusterInfoCollector;

  public ChaosMonkeyService(Configuration conf, ClusterInfoCollector clusterInfoCollector) throws Exception {
    this.processTable = HashBasedTable.create();
    this.conf = conf;
    this.clusterInfoCollector = clusterInfoCollector;
  }

  private SshShell resolveSshShell(Configuration conf, String ipAddress) throws JSchException {
    String username = conf.get("username", System.getProperty("user.name"));
    String privateKey = conf.get("privateKey");
    String keyPassphrase = conf.get("keyPassphrase");

    SshShell sshShell;
    if (privateKey != null) {
      if (keyPassphrase != null) {
        sshShell = new SshShell(username, ipAddress, privateKey, keyPassphrase);
      } else {
        sshShell = new SshShell(username, ipAddress, privateKey);
      }
    } else {
      sshShell = new SshShell(username, ipAddress);
    }

    return sshShell;
  }

  /**
   * Executes an action on configured processes
   *
   * @param service Name of the processes to be disrupted
   * @param action {@link Action} to be executed
   * @param actionArguments Configuration for the action to be run
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  public void executeAction(String service, Action action, @Nullable ActionArguments actionArguments) {
    Collection<RemoteProcess> processes = processTable.column(service).values();
    if (actionArguments == null) {
      actionArguments = new ActionArguments();
    }
    actionArguments.validate();

    if (actionArguments.getNodes() != null) {
      processes = new HashSet<>();
      List<String> invalidNodes = new ArrayList<>();
      for (String nodeIp : actionArguments.getNodes()) {
        RemoteProcess process = processTable.get(nodeIp, service);
        if (process == null) {
          invalidNodes.add(nodeIp);
        } else {
          processes.add(process);
        }
      }
      if (!invalidNodes.isEmpty()) {
        throw new BadRequestException("The following nodes do not exist, or they do not " +
                                    "support " + service + ": " + invalidNodes);
      }
    } else if (actionArguments.getCount() != null) {
      List<RemoteProcess> processList = new ArrayList<>(processTable.column(service).values());
      Collections.shuffle(processList);
      processes = new HashSet<>(processList.subList(0, Math.min(processList.size(), actionArguments.getCount())));
    } else if (actionArguments.getPercentage() != null) {
      List<RemoteProcess> processList = new ArrayList<>(processTable.column(service).values());
      Collections.shuffle(processList);
      processes = new HashSet<>(processList.subList(0, (int) Math.round(processList.size() *
                                                                          (actionArguments.getPercentage() / 100))));
    }

    if (processes.size() == 0) {
      throw new NotFoundException("Unknown service: " + service);
    }
    
    disruptionService.disrupt(action, service, processes, actionArguments.getRestartTime(), actionArguments.getDelay());
  }

  /**
   * Get the running status of a disruption
   *
   * @param service the name of the service to be queried
   * @param action the name of the action to be queried
   * @return {@link ActionStatus}
   */
  public ActionStatus getActionStatus(String service, String action) {
    return new ActionStatus(service, action, disruptionService.isRunning(service, action));
  }

  /**
   * Get the status of services on a given node
   *
   * @param hostname hostname of the node to query
   * @return {@link NodeStatus}
   * @throws JSchException if there was an SSH error
   * @throws NotFoundException if the hostname does not exist or is not configured
   */
  public NodeStatus getNodeStatus(String hostname) throws JSchException {
    Collection<RemoteProcess> remoteProcesses = processTable.row(hostname).values();
    if (remoteProcesses.isEmpty()) {
      throw new NotFoundException("Unknown host: " + hostname);
    }
    NodeStatus status = new NodeStatus(hostname);
    for (RemoteProcess remoteProcess : remoteProcesses) {
      status.serviceStatus.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
    }
    return status;
  }

  /**
   * Get the status of serivces on all configured nodes
   *
   * @return Collection of {@link NodeStatus}
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public Collection<NodeStatus> getNodeStatuses() throws ExecutionException, InterruptedException {
    List<Status> threads = new ArrayList<>();
    for (String ip : processTable.rowKeySet()) {
      threads.add(new Status(ip, processTable.row(ip).values()));
    }
    List<Future<NodeStatus>> results = executor.invokeAll(threads);

    List<NodeStatus> statuses = new ArrayList<>();
    for (Future<NodeStatus> result : results) {
      statuses.add(result.get());
    }

    return statuses;
  }

  public Table<String, String, RemoteProcess> getProcessTable() {
    return this.processTable;
  }

  @Override
  protected void startUp() throws Exception {
    Multimap<String, String> processToIp = HashMultimap.create();

    for (ClusterNode node : clusterInfoCollector.getNodeProperties()) {
      for (String service : node.getServices()) {
        processToIp.put(service, node.getHost());
      }
    }

    for (String service : processToIp.keySet()) {
      String pidPath = conf.get(service + ".pidPath");
      if (pidPath == null) {
        LOG.warn("The following process does not have a pidPath and will be skipped: {}", service);
        continue;
      }

      for (String ipAddress : processToIp.get(service)) {
        SshShell sshShell = resolveSshShell(conf, ipAddress);

        RemoteProcess process;
        switch (conf.get(service + ".init.style", "sysv")) {
          case "sysv":
            process = new SysVRemoteProcess(service, pidPath, sshShell);
            break;
          case "custom":
            ImmutableMap.Builder<String, String> map = ImmutableMap.builder();

            for (String configOption : Constants.RemoteProcess.CONFIG_OPTIONS) {
              String optionKey = String.format("%s.init.%s", service, configOption);
              if (conf.get(optionKey) != null) {
                map.put(configOption, conf.get(optionKey));
              }
            }

            process = new CustomRemoteProcess(service, pidPath, sshShell, map.build());
            break;
          default:
            throw new IllegalArgumentException("The following process does not have a valid init.style: " + service);
        }
        processTable.put(ipAddress, service, process);
      }
    }
    this.disruptionService = new DisruptionService(processTable.columnKeySet());
    this.executor = Executors.newFixedThreadPool(processTable.rowKeySet().size());
  }

  @Override
  protected void shutDown() throws Exception {
    this.executor.shutdown();
  }

  @Override
  public void start(String service) throws Exception {
    executeAction(service, Action.START, null);
  }

  @Override
  public void start(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, Action.START, actionArguments);
  }

  @Override
  public void start(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, Action.START, actionArguments);
  }

  @Override
  public void start(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, Action.START, actionArguments);
  }

  @Override
  public void restart(String service) throws Exception {
    executeAction(service, Action.RESTART, null);
  }

  @Override
  public void restart(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, Action.RESTART, actionArguments);
  }

  @Override
  public void restart(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, Action.RESTART, actionArguments);
  }

  @Override
  public void restart(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, Action.RESTART, actionArguments);
  }

  @Override
  public void stop(String service) throws Exception {
    executeAction(service, Action.STOP, null);
  }

  @Override
  public void stop(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, Action.STOP, actionArguments);
  }

  @Override
  public void stop(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, Action.STOP, actionArguments);
  }

  @Override
  public void stop(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, Action.STOP, actionArguments);
  }

  @Override
  public void terminate(String service) throws Exception {
    executeAction(service, Action.TERMINATE, null);
  }

  @Override
  public void terminate(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, Action.TERMINATE, actionArguments);
  }

  @Override
  public void terminate(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, Action.TERMINATE, actionArguments);
  }

  @Override
  public void terminate(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, Action.TERMINATE, actionArguments);
  }

  @Override
  public void kill(String service) throws Exception {
    executeAction(service, Action.KILL, null);
  }

  @Override
  public void kill(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, Action.KILL, actionArguments);
  }

  @Override
  public void kill(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, Action.KILL, actionArguments);
  }

  @Override
  public void kill(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, Action.KILL, actionArguments);
  }

  @Override
  public void rollingRestart(String service) throws Exception {
    rollingRestart(service, null);
  }

  @Override
  public void rollingRestart(String service, @Nullable ActionArguments actionArguments) throws Exception {
    executeAction(service, Action.ROLLING_RESTART, actionArguments);
  }

  @Override
  public boolean isStartRunning(String service) throws Exception {
    return getActionStatus(service, Action.START.getCommand()).isRunning();
  }

  @Override
  public boolean isRestartRunning(String service) throws Exception {
    return getActionStatus(service, Action.RESTART.getCommand()).isRunning();
  }

  @Override
  public boolean isStopRunning(String service) throws Exception {
    return getActionStatus(service, Action.STOP.getCommand()).isRunning();
  }

  @Override
  public boolean isTerminateRunning(String service) throws Exception {
    return getActionStatus(service, Action.TERMINATE.getCommand()).isRunning();
  }

  @Override
  public boolean isKillRunning(String service) throws Exception {
    return getActionStatus(service, Action.KILL.getCommand()).isRunning();
  }

  @Override
  public boolean isRollingRestartRunning(String service) throws Exception {
    return getActionStatus(service, Action.ROLLING_RESTART.getCommand()).isRunning();
  }

  @Override
  public boolean isActionRunning(String service, Action action) throws Exception {
    return getActionStatus(service, action.getCommand()).isRunning();
  }

  @Override
  public void waitForRollingRestart(String service) throws Exception {
    while (isRollingRestartRunning(service)) {
      TimeUnit.SECONDS.sleep(1);
    }
  }

  @Override
  public Collection<NodeStatus> getAllStatuses() throws Exception {
    return getNodeStatuses();
  }

  @Override
  public NodeStatus getStatus(String ipAddress) throws Exception {
    return getNodeStatus(ipAddress);
  }

  /**
   * Callable that gets the status of configured processes on a node
   */
  public static class Status implements Callable<NodeStatus> {
    private final Collection<RemoteProcess> processes;
    private final String ip;

    Status(String ip, Collection<RemoteProcess> processes) {
      this.ip = ip;
      this.processes = processes;
    }

    @Override
    public NodeStatus call() throws Exception {
      NodeStatus status = new NodeStatus(ip);
      for (RemoteProcess remoteProcess : processes) {
        status.serviceStatus.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
      }
      return status;
    }
  }
}
