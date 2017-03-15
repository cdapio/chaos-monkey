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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Restarts given service on each node sequentially
 */
public class RollingRestart implements Disruption {
  private static final Logger LOG = LoggerFactory.getLogger(RollingRestart.class);

  @Override
  public void disrupt(List<RemoteProcess> processes) throws Exception {
    disrupt(processes, null);
  }

  /**
   * Starts a rolling restart on given list of processes.
   *
   * @param processes         List of processes to rolling restart, must be of same type
   * @param actionArguments   Optional, configuration for delay and duration of rolling restart
   * @throws Exception
   */
  public void disrupt(List<RemoteProcess> processes, @Nullable ActionArguments actionArguments) throws Exception {
    if (processes.size() < 1) {
      throw new InvalidStateException("Process list has an invalid size of: " + processes.size());
    }

    int restartTime = (actionArguments == null || actionArguments.getRestartTime() == null ||
      actionArguments.getRestartTime() < 0) ? 30 : actionArguments.getRestartTime();
    int delay = (actionArguments == null || actionArguments.getDelay() == null ||
      actionArguments.getDelay() < 0) ? 120 : actionArguments.getDelay();

    for (RemoteProcess process : processes) {
      process.stop();
      TimeUnit.SECONDS.sleep(restartTime);
      process.start();
      TimeUnit.SECONDS.sleep(delay);
    }
  }

  @Override
  public String getName() {
    return "rolling-restart";
  }
}
