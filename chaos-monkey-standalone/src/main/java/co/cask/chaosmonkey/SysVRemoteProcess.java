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
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A remote process that ScheduledDisruption can interact with.
 */
public class SysVRemoteProcess implements RemoteProcess {
  private static final Logger LOG = LoggerFactory.getLogger(SysVRemoteProcess.class);

  protected final String name;
  protected final String pidFilePath;
  protected final SshShell sshShell;

  /**
   * Create a new {@code RemoteProcess}.
   *
   * @param name The name of the process on the remote host
   * @param pidFilePath The path to its pidfile on the remote host
   * @param sshShell The {@code SshShell} that should be used to execute remote commands on the remote
   */
  public SysVRemoteProcess(String name, String pidFilePath, SshShell sshShell) {
    this.name = name;
    this.pidFilePath = pidFilePath;
    this.sshShell = sshShell;
  }

  @Override
  public int execAndGetReturnCode(String command) throws JSchException {
    return sshShell.exec(command).returnCode;
  }

  @Override
  public boolean execAndReturnSucessful(String command) throws JSchException {
    return execAndGetReturnCode(command) == 0;
  }

  private int signal(int signum) throws JSchException {
    LOG.debug("Sending signal {} to {} on {}@{}", signum, getName(), sshShell.getUsername(), sshShell.getAddress());
    return execAndGetReturnCode(String.format("sudo kill -%d $(< %s)", signum, this.pidFilePath));
  }

  private int serviceCommand(String command) throws JSchException {
    LOG.debug("Sending service {} to {} on {}@{}", command, getName(), sshShell.getUsername(), sshShell.getAddress());
    return execAndGetReturnCode(String.format("sudo service %s %s", this.name, command));
  }

  public String getName() {
    return this.name;
  }

  public String getAddress() {
    return this.sshShell.getAddress();
  }

  /**
   * Starts the process using {@code service <name> start}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  @Override
  public int start() throws JSchException {
    return serviceCommand("start");
  }

  /**
   * Restarts the process using {@code service <name> restart}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  @Override
  public int restart() throws JSchException {
    return serviceCommand("restart");
  }

  /**
   * Stops the process using {@code service <name> stop}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  @Override
  public int stop() throws JSchException {
    return serviceCommand("stop");
  }

  /**
   * Terminates the process by sending it SIGTERM.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  @Override
  public int terminate() throws JSchException {
    return signal(Constants.RemoteProcess.SIGTERM);
  }

  /**
   * Kills the process by sending it SIGKILL.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  @Override
  public int kill() throws JSchException {
    return signal(Constants.RemoteProcess.SIGKILL);
  }

  /**
   * Returns whether the process is running.
   *
   * @return {@code true} if running, otherwise {@code false}.
   * @throws JSchException
   */
  @Override
  public boolean isRunning() throws JSchException {
    LOG.debug("Checking the status of {} on {}@{}", getName(), sshShell.getUsername(), sshShell.getAddress());
    return execAndReturnSucessful(String.format("sudo service %s status", this.name));
  }

  /**
   * Returns whether the process exists on a remote {@code SshShell}.
   *
   * @return {@code true} if the process exists, otherwise {@code false}
   */
  @Override
  public boolean exists() throws JSchException {
    LOG.debug("Checking if {} exists", getName());
    return execAndReturnSucessful(String.format("test -e /etc/init.d/%s", getName()));
  }
}
