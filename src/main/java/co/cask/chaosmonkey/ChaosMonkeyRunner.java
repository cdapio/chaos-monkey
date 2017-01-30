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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.JSchException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * The main runner for ChaosMonkey.
 */
public class ChaosMonkeyRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyRunner.class);
  private static final Gson GSON = new Gson();
  private static final Type NODES_TYPE = new TypeToken<Map<String, NodeProperties>>() { }.getType();

  private final Map<String, ChaosMonkeyService> processToChaosMonkeyService;

  /**
   * Creates a new {@code ChaosMonkeyRunner} object.
   *
   * @param processToChaosMonkeyService A {@code Map} from process name to {@code ChaosMonkeyService}
   */
  public ChaosMonkeyRunner(Map<String, ChaosMonkeyService> processToChaosMonkeyService) {
    this.processToChaosMonkeyService = processToChaosMonkeyService;
  }

  /**
   * Starts all services associated with this object.
   */
  public void startServices() {
    for (Map.Entry<String, ChaosMonkeyService> entry : processToChaosMonkeyService.entrySet()) {
      entry.getValue().startAsync();
    }
  }

  /**
   * Gets the Map of NodeProperties for each node in a given cluster
   * @return Map of NodeProperties
   * @throws IOException If an invalid cluster ID is given
   */
  public static Map<String, NodeProperties> getNodeProperties(Configuration conf) throws IOException {
    HttpClient client = new DefaultHttpClient();

    String clusterId = conf.get(Constants.Coopr.CLUSTER_ID);
    if (clusterId == null || clusterId.isEmpty()) {
      throw new IllegalArgumentException("Cluster ID not specified");
    }

    HttpPost httpPost = new HttpPost(conf.get(Constants.Coopr.SERVER_URI) + "/" +
                                       conf.get(Constants.Coopr.API_VERSION) + "/" +
                                       "getNodeProperties");
    httpPost.setHeader("coopr-userid", conf.get(Constants.Coopr.USER_ID));
    httpPost.setHeader("coopr-tenantid", conf.get(Constants.Coopr.TENANT_ID));
    httpPost.setEntity(new ByteArrayEntity(String.format("{\"clusterId\":\"%s\"}", clusterId)
                                             .getBytes("UTF-8")));

    HttpResponse response = client.execute(httpPost);

    Map<String, NodeProperties> nodes;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
       nodes = GSON.fromJson(reader, NODES_TYPE);
    }

    return nodes;
  }

  /**
   * The main method for this class.
   *
   * @param args
   * @throws JSchException if a SSH-related error occurs
   * @throws IllegalArgumentException if an invalid configuration file is given
   * @throws IOException if there was an error getting cluster information from Coopr
   */
  public static void main(String[] args) throws JSchException, IllegalArgumentException, IOException {
    Configuration conf = Configuration.create();

    String username = conf.get("username", System.getProperty("user.name"));
    String privateKey = conf.get("privateKey");
    String keyPassphrase = conf.get("keyPassphrase");

    Multimap<String, String> processToIp = HashMultimap.create();
    Map<String, ChaosMonkeyService> processToChaosMonkeyService = new HashMap<>();
    Collection<NodeProperties> propertiesList = ChaosMonkeyRunner.getNodeProperties(conf).values();

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
        LOGGER.warn("The following process does not have a valid interval and will be skipped: {}", service);
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

            for (String configOption : Constants.CustomRemoteProcess.CONFIG_OPTIONS) {
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
      }

      LOGGER.info("Adding the following process to Chaos Monkey: {}", service);
      ChaosMonkeyService chaosMonkeyService = new ChaosMonkeyService(processes, stopProbability, killProbability,
                                                                     restartProbability, interval,
                                                                     minNodesPerIteration, maxNodesPerIteration);
      processToChaosMonkeyService.put(service, chaosMonkeyService);
    }

    ChaosMonkeyRunner runner = new ChaosMonkeyRunner(processToChaosMonkeyService);
    runner.startServices();
    // TODO: Start the HTTP service here
  }
}
