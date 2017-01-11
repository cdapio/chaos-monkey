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

import java.util.concurrent.TimeUnit;

/**
 * TODO: Fill out this description
 */
public class ChaosMonkeyCLIService extends AbstractScheduledService {

  private Service[] processes;
  private double termFactor;
  private double killFactor;
  private int executionPeriod;
  private CommandService commandService;

  public ChaosMonkeyCLIService(Service[] processes,
                               double termFactor,
                               double killFactor,
                               int executionPeriod,
                               Shell shell) {
    this.processes = processes;
    this.termFactor = termFactor;
    this.killFactor = killFactor;
    this.executionPeriod = executionPeriod;
    this.commandService = new CommandService(shell);
  }

  public ChaosMonkeyCLIService(Service[] processes, double termFactor, double killFactor, int executionPeriod) {
    this(processes, termFactor, killFactor, executionPeriod, new Shell());
  }

  public ChaosMonkeyCLIService(Service[] processes, double termFactor, double killFactor) {
    this(processes, termFactor, killFactor, 1);
  }

  @Override
  protected void runOneIteration() throws Exception {
    for (Service process : processes) {
      double random = Math.random();// insecure but we don't need to worry about secure random numbers
      if (random < killFactor) {
        commandService.killProcess(process);
      } else if (random < termFactor) {
        commandService.terminateProcess(process);
      }
    }
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }
}
