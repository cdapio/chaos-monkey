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
public class ChaosMonkeyCLIService extends AbstractScheduledService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyCLIService.class);

  private Process process;
  private double termFactor;
  private double killFactor;
  private int executionPeriod;
  private ProcessHandler processHandler;

  /**
   *
   * @param process The processes that will be managed
   * @param termFactor The probability that a process will be terminated on each execution cycle
   * @param killFactor The probability that a process will be killed on each execution cycle
   * @param executionPeriod The rate of execution cycles (in seconds)
   * @param shell The shell which will be used to issue commands
   */
  public ChaosMonkeyCLIService(Process process,
                               double termFactor,
                               double killFactor,
                               int executionPeriod,
                               Shell shell) {
    this.process = process;
    this.termFactor = termFactor;
    this.killFactor = killFactor;
    this.executionPeriod = executionPeriod;
    this.processHandler = new ProcessHandler(shell);
  }

  @Override
  protected void runOneIteration() throws Exception {
    if (Math.random() < killFactor) {
      processHandler.killProcess(process);
    } else if (Math.random() < termFactor) {
      processHandler.stopProcess(process);
    }
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }
}
