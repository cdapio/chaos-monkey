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

/**
 * Restarts given service on each node sequentially
 */
public class RollingRestart implements Disruption {
  private static final Logger LOGGER = LoggerFactory.getLogger(RollingRestart.class);

  @Override
  public void disrupt(List<RemoteProcess> processes) throws Exception {
    disrupt(processes, 2 * 60 * 1000);
  }

  public void disrupt(List<RemoteProcess> processes, int delay) throws Exception {
    disrupt(processes, 30 * 1000, delay);
  }

  public void disrupt(List<RemoteProcess> processes, int restartTime, int delay) throws Exception {
    for (RemoteProcess process : processes) {
      process.stop();

      try {
        Thread.sleep(restartTime);
      } catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

      process.start();

      try {
        Thread.sleep(delay);
      } catch(InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public String getName() {
    return "rolling-restart";
  }
}
