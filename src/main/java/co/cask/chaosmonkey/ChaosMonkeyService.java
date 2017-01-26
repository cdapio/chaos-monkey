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

import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * The main service that will be running ChaosMonkey.
 */
public class ChaosMonkeyService extends AbstractScheduledService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyService.class);

  private RemoteProcess process;
  private double stopProbability;
  private double killProbability;
  private double restartProbability;
  private int executionPeriod;

  /**
   *
   * @param process The processes that will be managed
   * @param stopProbability Probability that this process will be stopped in the current interval
   * @param killProbability Probability that this process will be killed in the current interval
   * @param restartProbability Probability that this process will be restarted in the current interval
   * @param executionPeriod The rate of execution cycles (in seconds)
   */
  public ChaosMonkeyService(RemoteProcess process,
                            double stopProbability,
                            double killProbability,
                            double restartProbability,
                            int executionPeriod) {
    this.process = process;
    this.stopProbability = stopProbability;
    this.killProbability = killProbability;
    this.restartProbability = restartProbability;
    this.executionPeriod = executionPeriod;
  }

  @Override
  protected void runOneIteration() throws Exception {
    double random = Math.random();

    boolean serviceRunningBeforeIteration = process.isRunning();
    if (random < stopProbability && serviceRunningBeforeIteration) {
      LOGGER.info("Attempting to stop {}", process.getName());
      process.stop();
    } else if (random < stopProbability + killProbability && serviceRunningBeforeIteration) {
      LOGGER.info("Attempting to kill {}", process.getName());
      process.kill();
    } else if (random < stopProbability + killProbability + restartProbability && !serviceRunningBeforeIteration) {
      LOGGER.info("Attempting to restart {}", process.getName());
      process.restart();
    } else {
      return;
    }

    // Only do a check after an action has been attempted
    boolean serviceRunningAfterIteration = process.isRunning();
    if (serviceRunningBeforeIteration && serviceRunningAfterIteration) {
      LOGGER.error("{} is still running!", process.getName());
    } else if (serviceRunningBeforeIteration && !serviceRunningAfterIteration) {
      LOGGER.info("{} is no longer running", process.getName());
    } else if (!serviceRunningBeforeIteration && serviceRunningAfterIteration) {
      LOGGER.info("{} is now running", process.getName());
    } else if (!serviceRunningBeforeIteration && !serviceRunningAfterIteration) {
      LOGGER.error("{} did not restart", process.getName());
    }

  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }
}
