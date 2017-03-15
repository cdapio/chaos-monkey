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

import co.cask.chaosmonkey.common.Constants;
import co.cask.http.HttpResponder;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.jcraft.jsch.JSchException;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service to keep track of running disruptions
 */
public class DisruptionService {

  private Table<String, String, AtomicBoolean> status;
  private RollingRestart rollingRestart;

  public DisruptionService(Set<String> services) {
    status = HashBasedTable.create();
    for (String service : services) {
      for (String action : Constants.Action.ACTIONS) {
        status.put(service, action, new AtomicBoolean(false));
      }
    }
    rollingRestart = new RollingRestart();
  }

  public boolean isRunning(String service, String action) {
    if (status.get(service, action) == null) {
      return false;
    }
    return status.get(service, action).get();
  }

  public void disrupt(String action, String service, Collection<RemoteProcess> processes,
                      ActionArguments actionArguments, HttpResponder responder) throws Exception {
    if (checkAndStart(service, action)) {
      try {
        if (action.equals("rolling-restart")) {
          responder.sendString(HttpResponseStatus.OK, "Starting rolling restart");
          this.rollingRestart.disrupt(new ArrayList<>(processes), actionArguments);
          return;
        }

        for (RemoteProcess remoteProcess : processes) {
          try {
            switch (action) {
              case Constants.RemoteProcess.STOP:
                remoteProcess.stop();
                break;
              case Constants.RemoteProcess.KILL:
                remoteProcess.kill();
                break;
              case Constants.RemoteProcess.TERMINATE:
                remoteProcess.terminate();
                break;
              case Constants.RemoteProcess.START:
                remoteProcess.start();
                break;
              case Constants.RemoteProcess.RESTART:
                remoteProcess.restart();
                break;
              default:
                responder.sendString(HttpResponseStatus.NOT_FOUND, "Unknown command: " + action);
                return;
            }
          } catch (JSchException e) {
            responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            return;
          }
        }
      } finally {
        release(service, action);
      }
    } else {
      responder.sendString(HttpResponseStatus.CONFLICT, action + " is already running for: " + service);
      return;
    }
    responder.sendString(HttpResponseStatus.OK, "success");
  }

  private boolean checkAndStart(String service, String action) {
    AtomicBoolean atomicBoolean = status.get(service, action);
    return atomicBoolean.compareAndSet(false, true);
  }

  private void release(String service, String action) {
    status.put(service, action, new AtomicBoolean(false));
  }
}
