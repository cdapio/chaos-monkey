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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Restarts given service on each node sequentially
 */
public class RollingRestart implements Disruption {
  private static final Logger LOG = LoggerFactory.getLogger(RollingRestart.class);
  private static final Start start = new Start();
  private static final Stop stop = new Stop();

  /**
   * Starts a rolling restart on given list of processes.
   *
   * @param processes List of processes to rolling restart, must be of same type
   * @param serviceArguments Optional, configuration for delay and duration of rolling restart
   * @throws Exception
   */
  @Override
  public void disrupt(Collection<RemoteProcess> processes, @Nullable Map<String, String> serviceArguments)
    throws Exception {
    if (serviceArguments == null) {
      disrupt(processes, null, null);
    } else {
      Integer restartTime = serviceArguments.get("restartTime") == null ? null :
        new Integer(serviceArguments.get("restartTime"));
      Integer delay = serviceArguments.get("delay") == null ? null : new Integer(serviceArguments.get("delay"));
      disrupt(processes, restartTime, delay);
    }
  }

  /**
   * Starts a rolling restart on given list of processes.
   *
   * @param processes List of processes to rolling restart, must be of same type
   * @param restartTime Optional, number of seconds a service is down before restarting
   * @param delay Optional, number of seconds between restarting service on different nodes
   * @throws Exception
   */
  public void disrupt(Collection<RemoteProcess> processes, @Nullable Integer restartTime,
                      @Nullable Integer delay) throws Exception {
    if (processes.size() < 1) {
      throw new IllegalArgumentException("Process list has an invalid size of: " + processes.size());
    }

    restartTime = (restartTime == null || restartTime < 0) ? 30 : restartTime;
    delay = (delay == null || delay < 0) ? 120 : delay;

    for (RemoteProcess process : processes) {
      stop.disrupt(Arrays.asList(process), null);
      TimeUnit.SECONDS.sleep(restartTime);
      start.disrupt(Arrays.asList(process), null);
      TimeUnit.SECONDS.sleep(delay);
    }
  }

  @Override
  public String getName() {
    return "rolling-restart";
  }
}
