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

/**
 * Stores the chaos monkey interaction rules for a process
 */
public class ProcessRule {

  private final Process process;
  private final double killProbability;
  private final double stopProbability;
  private final double restartProbability;
  private final int interval;

  public ProcessRule(Process process, double killProbability, double stopProbability,
                     double restartProbability, int interval) {
    this.process = process;
    this.killProbability = killProbability;
    this.stopProbability = stopProbability;
    this.restartProbability = restartProbability;
    this.interval = interval;
  }

  /**
   * Get the process on which rules are applied
   * @return The process on which rules are applied
   */
  public Process getProcess() {
    return process;
  }

  /**
   * Get the probability of killing this process each execution cycle. Value ranges from 0 to 1
   * @return probability of killing this process
   */
  public double getKillProbability() {
    return killProbability;
  }

  /**
   * Get the probability of stopping this process each execution cycle. Value ranges from 0 to 1
   * @return probability of stopping this process
   */
  public double getStopProbability() {
    return stopProbability;
  }

  /**
   * Get the probability of restarting this process each execution cycle. Value ranges from 0 to 1.
   * @return probability of restarting this process
   */
  public double getRestartProbability() {
    return restartProbability;
  }

  /**
   * Get the interval value between each execution cycle
   * @return Interval value between each execution cycle
   */
  public int getInterval() {
    return interval;
  }
}
