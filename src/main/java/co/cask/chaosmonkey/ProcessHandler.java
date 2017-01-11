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
import java.util.ArrayList;

/**
 * TODO: procrastinate on documentation
 */
public class ProcessHandler {

  private static Shell shell;

  public ProcessHandler(Shell shell) {
    this.shell = shell;
  }

  /**
   * Finds currently running processes
   * @return ArrayList of running processes
   * @throws IOException
   */
  public ArrayList<Service> getRunningProcesses() throws IOException {
    ArrayList<Service> running = new ArrayList<>();
    for (Service service: Service.values()) {
      if (getPID(service.getPath()) >= 0) {
        running.add(service);
      }
    }
    return running;
  }

  /**
   * Finds the process ID of a service and then kill it
   * @param service The service to kill
   * @return Exit code
   * @throws IOException
   */
  public int killProcess(Service service) throws IOException {
    return signalProcess(9, service);
  }

  /**
   * Finds the process ID of a service and then terminate it
   * @param service The service to terminate
   * @return Exit code
   * @throws IOException
   */
  public int terminateProcess(Service service) throws IOException {
    return signalProcess(15, service);
  }

  private int signalProcess(int signal, Service service) throws IOException {
    int pid = getPID(service.getPath());
    return (pid >= 0) ? signalProcessWithPID(signal, pid) : pid;
  }

  private int getPID(String pathToPID) throws IOException {
    // TODO: Refactor this to use File instead, maybe?
    String command = String.format("cat %s", pathToPID);
    ShellOutput output = shell.exec(command);
    return (output.returnCode >= 0) ? Integer.parseInt(output.standardOutput) : -1;
  }

  private int signalProcessWithPID(int signal, int pid) throws IOException {
    String command = String.format("kill -%d %d", signal, pid);
    ShellOutput output = shell.exec(command);
    return output.returnCode;
  }
}
