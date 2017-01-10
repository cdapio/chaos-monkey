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
   * Finds the process ID of a service and then kill it
   * @param service The service to kill
   * @return Exit code
   * @throws IOException
   */
  public int killProcess(Service service) throws IOException {
    int pid = getPID(service.getPath());
    if (pid == -1) {
      throw new IOException("Process ID not found");
    }
    return signalProcess(9, pid);
  }

  /**
   * Finds the process ID of a service and then terminate it
   * @param service The service to terminate
   * @return Exit code
   * @throws IOException
   */
  public int terminateProcess(Service service) throws IOException {
    int pid = getPID(service.getPath());
    if (pid == -1) {
      throw new IOException("Process ID not found");
    }
    return signalProcess(15, pid);
  }

  private int getPID(String pathToPID) throws IOException {
    String command = String.format("cat %s", pathToPID);
    ShellOutput output = shell.exec(command);
    if (output.returnCode == 0) {
      return Integer.parseInt(output.standardOutput);
    }
    return -1;
  }

  private int signalProcess(int signal, int pid) throws IOException {
    String command = String.format("kill -%d %d", signal, pid);
    ShellOutput output = shell.exec(command);
    return output.returnCode;
  }
}
