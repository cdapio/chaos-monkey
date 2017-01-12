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
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * TODO: Fill out this description
 */
public class ChaosMonkeyCLIService extends AbstractScheduledService {
  private static final Logger LOGGER =Logger.getLogger(ChaosMonkeyCLIService.class);

  private Service[] processes;
  private double termFactor;
  private double killFactor;
  private int executionPeriod;
  private ProcessHandler processHandler;

  public ChaosMonkeyCLIService(Service[] processes,
                               double termFactor,
                               double killFactor,
                               int executionPeriod,
                               Shell shell) {
    this.processes = processes;
    this.termFactor = termFactor;
    this.killFactor = killFactor;
    this.executionPeriod = executionPeriod;
    this.processHandler = new ProcessHandler(shell);
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
      double randomActionNumber = Math.random();// insecure but we don't need to worry about secure random numbers
      LOGGER.debug("randomActionNumber generated: " + randomActionNumber);
      if (randomActionNumber < killFactor) {
        // TODO: don't know if info is the right level for this
        LOGGER.info("Killing service: " + process.getName());
        processHandler.killProcess(process);
      } else if (randomActionNumber < termFactor) {
        // TODO: don't know if info is the right level for this
        LOGGER.info("Killing service: " + process.getName());
        processHandler.terminateProcess(process);
      }
    }
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }
}
