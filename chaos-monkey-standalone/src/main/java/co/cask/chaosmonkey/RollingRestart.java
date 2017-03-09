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

import co.cask.http.HttpResponder;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Restarts given service on each node sequentially
 */
public class RollingRestart implements Disruption {
  private static final Logger LOG = LoggerFactory.getLogger(RollingRestart.class);

  private Map<String, Boolean> running;

  public RollingRestart() {
    running = new HashMap<>();
  }

  @Override
  public void disrupt(List<RemoteProcess> processes) throws Exception {
    disrupt(processes, null, null);
  }

  /**
   * Starts a rolling restart on given list of processes.
   *
   * @param processes         List of processes to rolling restart, must be of same type
   * @param actionArguments   Optional, configuration for delay and duration of rolling restart
   * @throws Exception
   */
  public void disrupt(List<RemoteProcess> processes, @Nullable ActionArguments actionArguments) throws Exception {
    disrupt(processes, actionArguments, null);
  }

  /**
   * Starts a rolling restart on given list of processes. If HttpResponder is provided, it will send corresponding
   * response prior to starting rolling restart
   *
   * @param processes         List of processes to rolling restart, must be of same type
   * @param actionArguments   Optional, configuration for delay and duration of rolling restart
   * @param responder         Optional, will respond prior to starting rolling restart if provided
   * @throws Exception
   */
  public void disrupt(List<RemoteProcess> processes, @Nullable ActionArguments actionArguments,
                      @Nullable HttpResponder responder) throws Exception {
    if (processes.size() < 1) {
      throw new InvalidStateException("Process list has an invalid size of: " + processes.size());
    }
    String processName = processes.get(0).getName();
    int restartTime = (actionArguments == null || actionArguments.getRestartTime() == null ||
      actionArguments.getRestartTime() < 0) ? 30 : actionArguments.getRestartTime();
    int delay = (actionArguments == null || actionArguments.getDelay() == null ||
      actionArguments.getDelay() < 0) ? 120 : actionArguments.getDelay();

    if (running.containsKey(processName) && running.get(processName)) {
      if (responder != null) {
        responder.sendString(HttpResponseStatus.CONFLICT, processName + " is already undergoing rolling restart");
        return;
      }
      throw new InvalidStateException("Rolling Restart already running");
    }
    if (responder != null) {
      responder.sendString(HttpResponseStatus.OK, "Starting rolling restart");
    }
    running.put(processName, true);
    try {
      for (RemoteProcess process : processes) {
        process.stop();
        TimeUnit.SECONDS.sleep(restartTime);
        process.start();
        TimeUnit.SECONDS.sleep(delay);
      }
    } finally {
      running.put(processName, false);
    }
  }

  @Override
  public String getName() {
    return "rolling-restart";
  }

  public boolean isRunning(String processName) {
    if (running.get(processName) == null) {
      return false;
    }
    return running.get(processName);
  }
}
