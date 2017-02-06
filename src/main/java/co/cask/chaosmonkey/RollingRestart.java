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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Restarts given service on each node sequentially
 */
public class RollingRestart implements Disruption {
  private static final Logger LOG = LoggerFactory.getLogger(RollingRestart.class);

  private int restartTime;
  private int delay;

  public RollingRestart (ActionArguments actionArguments) {
    this.restartTime = (actionArguments == null || actionArguments.getRestartTime() == null ||
      actionArguments.getRestartTime() < 0) ? 30 : actionArguments.getRestartTime();
    this.delay = (actionArguments == null || actionArguments.getDelay() == null ||
      actionArguments.getDelay() < 0) ? 120 : actionArguments.getDelay();
  }

  @Override
  public void disrupt(List<RemoteProcess> processes) throws Exception {
    for (RemoteProcess process : processes) {
      process.stop();
      TimeUnit.SECONDS.sleep(this.restartTime);
      process.start();
      TimeUnit.SECONDS.sleep(this.delay);
    }
  }

  @Override
  public String getName() {
    return "rolling-restart";
  }
}
