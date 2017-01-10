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

import java.io.IOException;

/**
 * TODO: procrastinate on documentation
 */
public class KillCommand {

  private static Shell shell;

  public KillCommand(Shell shell) {
    this.shell = shell;
  }

  /**
   * TODO: procrastinate on documentation
   * @param processName
   * @return
   * @throws IOException
   */
  public int killProcess(String processName) throws IOException {
    int pid = getPID(processName);
    return signalProcess(9, pid);
  }

  /**
   * TODO: procrastinate on documentation
   * @param processName
   * @return
   * @throws IOException
   */
  public int terminateProcess(String processName) throws IOException {
    int pid = getPID(processName);
    return signalProcess(15, pid);
  }

  public int getPID(String processName) throws IOException {
    String command = String.format("ps aux | grep %s | grep -v grep | awk '{print $2}'", processName);
    ShellOutput output = shell.exec(command);

    // TODO: better interpret output and deal with getting multiple PID
    String[] splitOutput = output.standardOutput.split("\n");
    return Integer.parseInt(splitOutput[0]);
  }

  private int signalProcess(int signal, int pid) throws IOException {
    String command = String.format("kill -%d %d", signal, pid);
    ShellOutput output = shell.exec(command);
    return output.returnCode;
  }
}
