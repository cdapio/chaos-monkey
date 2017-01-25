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

import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A remote process that ChaosMonkey can interact with.
 */
public interface RemoteProcess {

  /**
   * Returns this process's name.
   *
   * @return The name of this process.
   */
   String getName();

  /**
   * Executes a command and returns the return code.
   *
   * @param command The command to be executed
   * @return The return code after running the command
   * @throws JSchException
   */
   int execAndGetReturnCode(String command) throws JSchException;

  /**
   * Executes a command and returns if it was successful.
   *
   * @param command The command to be executed
   * @return {@code true} if the command was sucessful, otherwise {@code false}
   * @throws JSchException
   */
   boolean execAndReturnSucessful(String command) throws JSchException;

  /**
   * Starts the process.
   *
   * @return The return code from running the process.
   */
   int start() throws JSchException;

  /**
   * Restarts the process.
   *
   * @return The return code from running the process.
   */
   int restart() throws JSchException;

  /**
   * Stops the process.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
   int stop() throws JSchException;

  /**
   * Terminates the process by sending it SIGTERM.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
   int terminate() throws JSchException;

  /**
   * Kills the process by sending it SIGKILL.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
   int kill() throws JSchException;

  /**
   * Returns whether the process is running.
   *
   * @return {@code true} if running, otherwise {@code false}.
   * @throws JSchException
   */
   boolean isRunning() throws JSchException;

  /**
   * Returns whether the process exists on a remote {@code SshShell}.
   *
   * @return {@code true} if the process exists, otherwise {@code false}
   */
   boolean exists() throws JSchException;
}
