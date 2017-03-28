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
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The main runner for ScheduledDisruption.
 */
public class ChaosMonkeyMain extends DaemonMain {
  private static final Logger LOG = LoggerFactory.getLogger(ChaosMonkeyMain.class);

  private ChaosMonkeyService chaosMonkeyService;
  private ChaosMonkeyHttpService chaosMonkeyHttpService;
  private Set<ScheduledDisruption> scheduledDisruptionSet;
  private Configuration conf;

  public static void main(String[] args) throws Exception {
    new ChaosMonkeyMain().doMain(args);
  }

  @Override
  public void init(String[] args) {
    conf = Configuration.create();
    try {
      ClusterInfoCollector clusterInfoCollector = Clusters.createInitializedInfoCollector(conf);
      chaosMonkeyService = new ChaosMonkeyService(conf, clusterInfoCollector);
      chaosMonkeyHttpService = new ChaosMonkeyHttpService(chaosMonkeyService);

    } catch (ClassNotFoundException e) {
      LOG.error("Unable to instantiate cluster info collector class: " +
                  conf.get(Constants.Plugins.CLUSTER_INFO_COLLECTOR_CLASS));
      throw new RuntimeException(e);
    } catch (Throwable t) {
      LOG.error(t.getMessage(), t);
      throw new RuntimeException(t);
    }
  }

  // TODO: make DisruptionScheduler to initialize Scheduled Disurptions
  private void startScheduledServices() {
    scheduledDisruptionSet = new HashSet<>();
    Table<String, String, RemoteProcess> processTable = chaosMonkeyService.getProcessTable();

    for (String service : processTable.columnKeySet()) {
      boolean scheduled = true;
      int interval;
      try {
        interval = conf.getInt(service + ".interval");
        if (interval <= 0) {
          throw new IllegalArgumentException();
        }
      } catch (IllegalArgumentException | NullPointerException e) {
        LOG.warn("The following process does not have a valid interval and will be skipped: {}", service);
        interval = 0; // To avoid variable not initialized error, will not be used
        scheduled = false;
      }

      double killProbability = conf.getDouble(service + ".killProbability", 0.0);
      double stopProbability = conf.getDouble(service + ".stopProbability", 0.0);
      double restartProbability = conf.getDouble(service + ".restartProbability", 0.0);
      int minNodesPerIteration = conf.getInt(service + ".minNodesPerIteration", 0);
      int maxNodesPerIteration = conf.getInt(service + ".maxNodesPerIteration", 0);

      if (scheduled && killProbability == 0.0 && stopProbability == 0.0 && restartProbability == 0.0) {
        LOG.warn("The following process may have all of killProbability, stopProbability and restartProbability " +
                   "equal to 0.0 or undefined: {}", service);
        scheduled = false;
      }
      if (scheduled && stopProbability + killProbability + restartProbability > 1) {
        LOG.warn("The following process has a combined killProbability, stopProbability and restartProbability " +
                   "of over 1.0: {}", service);
        scheduled = false;
      }

      if (scheduled) {
        LOG.info("Adding the following process to Chaos Monkey: {}", service);
        ScheduledDisruption scheduledDisruption = new ScheduledDisruption(new ArrayList<>(processTable.column(service).values()),
                                                                          stopProbability, killProbability, restartProbability, interval,
                                                                          minNodesPerIteration, maxNodesPerIteration);
        scheduledDisruptionSet.add(scheduledDisruption);
      }
    }
  }

  @Override
  public void start() throws Exception {
    chaosMonkeyService.startAsync();
    chaosMonkeyHttpService.startAsync();
    chaosMonkeyService.awaitRunning();
    startScheduledServices();
    for (ScheduledDisruption scheduledDisruption : scheduledDisruptionSet) {
      scheduledDisruption.startAsync();
    }
  }

  @Override
  public void stop() {
    try {
      chaosMonkeyHttpService.shutDown();
      chaosMonkeyService.shutDown();
      for (ScheduledDisruption scheduledDisruption : scheduledDisruptionSet) {
        scheduledDisruption.stopAsync();
      }
    } catch (Exception e) {
      LOG.warn("Exception when trying to shut down Chaos Monkey.", e);
    }
  }

  @Override
  public void destroy() {
    // NO-OP
  }
}
