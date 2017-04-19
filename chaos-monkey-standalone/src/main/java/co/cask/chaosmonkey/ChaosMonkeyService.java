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
import co.cask.chaosmonkey.proto.ClusterDisruptor;
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import co.cask.chaosmonkey.proto.ClusterNode;
import co.cask.chaosmonkey.proto.NodeStatus;
import co.cask.chaosmonkey.proto.ServiceStatus;
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
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * {@link ChaosMonkeyService} Allows for user to perform disruptions directly
 */
public class ChaosMonkeyService extends AbstractIdleService implements ClusterDisruptor {
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
   * @param disruptionName Disruption to be executed
   * @param actionArguments Configuration for the action to be run
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  public void executeAction(String service, String disruptionName, @Nullable ActionArguments actionArguments) {
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
    
    disruptionService.disrupt(disruptionName, service, processes, actionArguments.getRestartTime(),
                              actionArguments.getDelay());
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
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws NotFoundException if the hostname does not exist or is not configured
   */
  public NodeStatus getNodeStatus(String hostname) throws ExecutionException, InterruptedException {
    Collection<RemoteProcess> remoteProcesses = processTable.row(hostname).values();
    if (remoteProcesses.isEmpty()) {
      throw new NotFoundException("Unknown host: " + hostname);
    }
    List<Status> threads = new ArrayList<>();
    for (RemoteProcess remoteProcess : processTable.row(hostname).values()) {
      threads.add(new Status(remoteProcess));
    }
    List<Future<ServiceStatus>> results = executor.invokeAll(threads);
    List<ServiceStatus> statuses = new ArrayList<>();

    for (Future<ServiceStatus> result : results) {
      statuses.add(result.get());
    }

    NodeStatus status = new NodeStatus(hostname, statuses);
    return status;
  }

  /**
   * Get the status of services on all configured nodes
   *
   * @return Collection of {@link NodeStatus}
   * @throws ExecutionException
   * @throws InterruptedException
   */
  public Collection<NodeStatus> getNodeStatuses() throws ExecutionException, InterruptedException {
    List<Status> threads = new ArrayList<>();
    List<NodeStatus> statuses = new ArrayList<>();

    for (RemoteProcess remoteProcess : processTable.values()) {
      threads.add(new Status(remoteProcess));
    }
    List<Future<ServiceStatus>> results = executor.invokeAll(threads);
    Multimap<String, ServiceStatus> serviceMap = HashMultimap.create();

    for (Future<ServiceStatus> result : results) {
      ServiceStatus status = result.get();
      serviceMap.put(status.getAddress(), status);
    }

    for (String address : serviceMap.keySet()) {
      statuses.add(new NodeStatus(address, serviceMap.get(address)));
    }

    return statuses;
  }

  public Table<String, String, RemoteProcess> getProcessTable() {
    return this.processTable;
  }

  @Override
  protected void startUp() throws Exception {
    Multimap<String, String> processToIp = HashMultimap.create();
    Table<String, String, Disruption> disruptionTable = HashBasedTable.create();

    for (ClusterNode node : clusterInfoCollector.getNodeProperties()) {
      for (String service : node.getServices()) {
        processToIp.put(service, node.getHost());
      }
    }

    for (String service : processToIp.keySet()) {
      String pidPath = conf.get(service + ".pidPath");
      String disruptionsConf = conf.get(service + ".disruptions");

      if (pidPath == null && disruptionsConf == null) {
        LOG.warn("The following process does not have a pidPath and will be skipped: {}", service);
        continue;
      }

      if (disruptionsConf == null) {
        disruptionsConf = Constants.RemoteProcess.DEFAULT_DISRUPTIONS;
      }
      String[] disruptions = disruptionsConf.split(",");
      for (String disruptionString : disruptions) {
        Disruption disruption = Class.forName(disruptionString).asSubclass(Disruption.class).newInstance();
        disruptionTable.put(service, disruption.getName(), disruption);
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
    this.disruptionService = new DisruptionService(disruptionTable);
    this.executor = Executors.newFixedThreadPool(processTable.values().size());
  }

  @Override
  protected void shutDown() throws Exception {
    this.executor.shutdown();
  }

  @Override
  public void start(String service) throws Exception {
    executeAction(service, "start", null);
  }

  @Override
  public void start(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, "start", actionArguments);
  }

  @Override
  public void start(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, "start", actionArguments);
  }

  @Override
  public void start(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, "start", actionArguments);
  }

  @Override
  public void restart(String service) throws Exception {
    executeAction(service, "restart", null);
  }

  @Override
  public void restart(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, "restart", actionArguments);
  }

  @Override
  public void restart(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, "restart", actionArguments);
  }

  @Override
  public void restart(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, "restart", actionArguments);
  }

  @Override
  public void stop(String service) throws Exception {
    executeAction(service, "stop", null);
  }

  @Override
  public void stop(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, "stop", actionArguments);
  }

  @Override
  public void stop(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, "stop", actionArguments);
  }

  @Override
  public void stop(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, "stop", actionArguments);
  }

  @Override
  public void terminate(String service) throws Exception {
    executeAction(service, "terminate", null);
  }

  @Override
  public void terminate(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, "terminate", actionArguments);
  }

  @Override
  public void terminate(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, "terminate", actionArguments);
  }

  @Override
  public void terminate(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, "terminate", actionArguments);
  }

  @Override
  public void kill(String service) throws Exception {
    executeAction(service, "kill", null);
  }

  @Override
  public void kill(String service, int count) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setCount(count).build();
    executeAction(service, "kill", actionArguments);
  }

  @Override
  public void kill(String service, double percentage) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setPercentage(percentage).build();
    executeAction(service, "kill", actionArguments);
  }

  @Override
  public void kill(String service, Collection<String> nodes) throws Exception {
    ActionArguments actionArguments = ActionArguments.builder().setNodes(nodes).build();
    executeAction(service, "kill", actionArguments);
  }

  @Override
  public void rollingRestart(String service) throws Exception {
    rollingRestart(service, null);
  }

  @Override
  public void rollingRestart(String service, @Nullable ActionArguments actionArguments) throws Exception {
    executeAction(service, "rolling-restart", actionArguments);
  }

  @Override
  public boolean isStartRunning(String service) throws Exception {
    return getActionStatus(service, "start").isRunning();
  }

  @Override
  public void startAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception {
    start(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isStartRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  @Override
  public boolean isRestartRunning(String service) throws Exception {
    return getActionStatus(service, "restart").isRunning();
  }

  @Override
  public void restartAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception {
    restart(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isRestartRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  @Override
  public boolean isStopRunning(String service) throws Exception {
    return getActionStatus(service, "stop").isRunning();
  }

  @Override
  public void stopAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception {
    stop(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isStopRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  @Override
  public boolean isTerminateRunning(String service) throws Exception {
    return getActionStatus(service, "terminate").isRunning();
  }

  @Override
  public void terminateAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception {
    terminate(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isTerminateRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  @Override
  public boolean isKillRunning(String service) throws Exception {
    return getActionStatus(service, "kill").isRunning();
  }

  @Override
  public void killAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception {
    kill(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isKillRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  @Override
  public boolean isRollingRestartRunning(String service) throws Exception {
    return getActionStatus(service, "rolling-restart").isRunning();
  }

  @Override
  public void rollingRestartAndWait(String service, @Nullable ActionArguments actionArguments) throws Exception {
    rollingRestart(service, actionArguments);
    while (isRollingRestartRunning(service)) {
      TimeUnit.SECONDS.sleep(1);
    }
  }

  @Override
  public boolean isActionRunning(String service, String action) throws Exception {
    return getActionStatus(service, action).isRunning();
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
   * Callable to return the status of a single service
   */
  public static class Status implements Callable<ServiceStatus> {

    private final RemoteProcess process;

    Status(RemoteProcess process) {
      this.process = process;
    }

    public ServiceStatus call() throws Exception {
      return new ServiceStatus(process.getAddress(), process.getName(), process.isRunning() ? "running" : "stopped");
    }
  }
}
