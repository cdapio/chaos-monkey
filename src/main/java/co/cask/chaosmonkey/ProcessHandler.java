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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * ProcessHandler contains actions/commands for the chaos monkey to execute on processes
 */
public class ProcessHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessHandler.class);

  private Shell shell;

  public ProcessHandler(Shell shell) {
    this.shell = shell;
  }

  /**
   * Finds currently running processes
   * @return Set of running processes
   * @throws IOException
   */
  public static Set<Process> getRunningProcesses() throws IOException {
    Set<Process> running = new HashSet<>();
    for (String processName : Process.PROCESS_MAP.keySet()) {
      Process process = Process.PROCESS_MAP.get(processName);
      if (process.getFile().exists() && process.getFile().canRead()) {
        running.add(process);
      }
    }
    return running;
  }

  /**
   * Finds the process ID of a process and then kill it
   * @param process The process to kill
   * @return Exit code
   * @throws IOException
   */
  public int killProcess(Process process) throws IOException {
    LOGGER.info("Killing process: " + process.getName());
    return signalProcess(9, process);
  }

  /**
   * Finds the process ID of a process and then stop it
   * @param process The process to stop
   * @return Exit code
   * @throws IOException
   */
  public int stopProcess(Process process) throws IOException {
    LOGGER.info("Stopping process: " + process.getName());
    return signalProcess(15, process);
  }

  /**
   * Finds the process ID of a process and then signal it with given value
   * @param signal  The UNIX signal to send to process
   * @param process The process to signal
   * @return Exit code
   * @throws IOException
   */
  public int signalProcess(int signal, Process process) throws IOException {
    int pid = getPIDFromFile(process.getFile());
    return (pid >= 0) ? signalProcessWithPID(signal, pid) : pid;
  }

  private static int getPIDFromFile(File file) {
    try (Scanner scanner = new Scanner(file)) {
      return scanner.nextInt();
    } catch (FileNotFoundException e) {
      LOGGER.warn("Unable to get process ID: " + file.getAbsolutePath() + " does not exist");
      return -1;
    }
  }

  private int signalProcessWithPID(int signal, int pid) throws IOException {
    String command = String.format("kill -%d %d", signal, pid);
    ShellOutput output = shell.exec(command);
    return output.returnCode;
  }
}
