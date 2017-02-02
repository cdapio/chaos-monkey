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

import co.cask.chaosmonkey.conf.Configuration;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;

/**
 * The main runner for ChaosMonkey.
 */
public class ChaosMonkeyMain extends DaemonMain {
  private static final Logger LOG = LoggerFactory.getLogger(ChaosMonkeyMain.class);

  private Router router;

  public static void main(String[] args) throws Exception {
      new ChaosMonkeyMain().doMain(args);
  }

  @Override
  public void init(String[] args) {
    try {
      Configuration conf = Configuration.create();

      String username = conf.get("username", System.getProperty("user.name"));
      String privateKey = conf.get("privateKey");
      String keyPassphrase = conf.get("keyPassphrase");

      Multimap<String, String> processToIp = HashMultimap.create();
      Multimap<String, RemoteProcess> ipToProcess = HashMultimap.create();
      Multimap<String, RemoteProcess> nameToProcess = HashMultimap.create();
      Collection<NodeProperties> propertiesList = ChaosMonkeyHelper.getNodeProperties(conf).values();

      for (NodeProperties node : propertiesList) {
        for (String service : node.getServices()) {
          processToIp.put(service, node.getAccessIpAddress());
        }
      }

      for (String service : processToIp.keySet()) {
        int interval;
        try {
          interval = conf.getInt(service + ".interval");
          if (interval <= 0) {
            throw new IllegalArgumentException();
          }
        } catch (IllegalArgumentException | NullPointerException e) {
          LOG.warn("The following process does not have a valid interval and will be skipped: {}", service);
          continue;
        }

        String pidPath = conf.get(service + ".pidPath");
        if (pidPath == null) {
          throw new IllegalArgumentException("The following process does not have a pidPath: " + service);
        }

        double killProbability = conf.getDouble(service + ".killProbability", 0.0);
        double stopProbability = conf.getDouble(service + ".stopProbability", 0.0);
        double restartProbability = conf.getDouble(service + ".restartProbability", 0.0);
        int minNodesPerIteration = conf.getInt(service + ".minNodesPerIteration", 0);
        int maxNodesPerIteration = conf.getInt(service + ".maxNodesPerIteration", 0);

        if (killProbability == 0.0 && stopProbability == 0.0 && restartProbability == 0.0) {
          throw new IllegalArgumentException("The following process may have all of killProbability, stopProbability " +
                                               "and restartProbability equal to 0.0 or undefined: " + service);
        }
        if (stopProbability + killProbability + restartProbability > 1) {
          throw new IllegalArgumentException("The following process has a combined killProbability, stopProbability " +
                                               "and restartProbability of over 1.0: " + service);
        }

        LinkedList<RemoteProcess> processes = new LinkedList<>();
        for (String ipAddress : processToIp.get(service)) {
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
          RemoteProcess process;
          switch (conf.get(service + ".init.style")) {
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
          processes.add(process);
          ipToProcess.put(ipAddress, process);
          nameToProcess.put(service, process);
        }

        LOG.info("Adding the following process to Chaos Monkey: {}", service);
        ChaosMonkeyService chaosMonkeyService = new ChaosMonkeyService(processes, stopProbability, killProbability,
                                                                       restartProbability, interval,
                                                                       minNodesPerIteration, maxNodesPerIteration);
        chaosMonkeyService.startAsync();
      }

      router = new Router(conf, ipToProcess, nameToProcess);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw new RuntimeException(t);
    }
  }

  @Override
  public void start() throws Exception {
    router.startAsync();
  }

  @Override
  public void stop() {
    try {
      router.shutDown();
    } catch (Exception e) {
      LOG.debug("Exception when trying to shut down server.", e);
    }
  }

  @Override
  public void destroy() {
    // NO-OP
  }
}
