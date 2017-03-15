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
 * Actions that can be performed by chaos monkey through the http handler
 */
public enum Action {
  START("start"),
  RESTART("restart"),
  STOP("stop"),
  TERMINATE("terminate"),
  KILL("kill"),
  ROLLING_RESTART("rolling-restart");

  private String command;

  Action(String command) {
    this.command = command;
  }

  public String getCommand() {
    return this.command;
  }
}
