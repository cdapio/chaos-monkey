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

package io.cdap.chaosmonkey.proto;

/**
 * Data to be sent when querying for rolling restart status
 */
public class ActionStatus {
  private String processName;
  private String actionName;
  private boolean isRunning;

  public ActionStatus(String processName, String actionName, boolean isRunning) {
    this.processName = processName;
    this.actionName = actionName;
    this.isRunning = isRunning;
  }

  public String getProcessName() {
    return this.processName;
  }

  public String getActionName() {
    return this.actionName;
  }

  public boolean isRunning() {
    return this.isRunning;
  }
}
