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

package io.cdap.chaosmonkey;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Scheduled service that will periodically disrupt configured services
 */
public class ScheduledDisruption extends AbstractScheduledService {
  private static final Logger LOG = LoggerFactory.getLogger(ScheduledDisruption.class);

  private List<RemoteProcess> processes;
  private double stopProbability;
  private double killProbability;
  private double restartProbability;
  private int executionPeriod;
  private int minNodesPerIteration;
  private int maxNodesPerIteration;
  private Kill kill = new Kill();
  private Stop stop = new Stop();
  private Restart restart = new Restart();

  /**
   *
   * @param processes A list of processes that will be managed
   * @param stopProbability Probability that this process will be stopped in the current interval
   * @param killProbability Probability that this process will be killed in the current interval
   * @param restartProbability Probability that this process will be restarted in the current interval
   * @param executionPeriod The rate of execution cycles (in seconds)
   * @param minNodesPerIteration The minimum number of nodes that will be affected by chaos monkey each iteration
   * @param maxNodesPerIteration The maximum number of nodes that will be affected by chaos monkey each iteration
   */
  public ScheduledDisruption(List<RemoteProcess> processes,
                             double stopProbability,
                             double killProbability,
                             double restartProbability,
                             int executionPeriod,
                             int minNodesPerIteration,
                             int maxNodesPerIteration) {
    this.processes = processes;
    this.stopProbability = stopProbability;
    this.killProbability = killProbability;
    this.restartProbability = restartProbability;
    this.executionPeriod = executionPeriod;

    this.minNodesPerIteration = Math.min(processes.size(), minNodesPerIteration);
    this.maxNodesPerIteration = Math.min(processes.size(), maxNodesPerIteration);
    if (this.maxNodesPerIteration < 0) {
      this.maxNodesPerIteration = processes.size() + this.maxNodesPerIteration;
    }
    if (this.minNodesPerIteration < 0) {
      this.minNodesPerIteration = processes.size() + this.minNodesPerIteration;
    }
    if (this.minNodesPerIteration > this.maxNodesPerIteration) {
      throw new IllegalArgumentException("minNodePerIteration is greater than maxNodePerIteration for process: "
                                            + this.processes.get(0).getName() + "\n" +
                                            "minNodePerIteration: " + this.minNodesPerIteration + "\n" +
                                            "maxNodePerIteration: " + this.maxNodesPerIteration);
    }
  }

  @Override
  protected void runOneIteration() throws Exception {
    double random = Math.random();
    int numNodes = ThreadLocalRandom.current().nextInt(minNodesPerIteration, maxNodesPerIteration + 1);

    if (random < stopProbability) {
      stop.disrupt(getAffectedNodes(numNodes), null);
    } else if (random < stopProbability + killProbability) {
      kill.disrupt(getAffectedNodes(numNodes), null);
    } else if (random < stopProbability + killProbability + restartProbability) {
      restart.disrupt(getAffectedNodes(numNodes), null);
    } else {
      return;
    }
  }

  private List<RemoteProcess> getAffectedNodes(int numNodes) {
    Collections.shuffle(processes);
    return processes.subList(0, numNodes);
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }
}
