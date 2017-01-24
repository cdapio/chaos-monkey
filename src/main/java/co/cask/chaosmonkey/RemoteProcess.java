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
public class RemoteProcess {
  private static final Logger LOG = LoggerFactory.getLogger(RemoteProcess.class);

  private final String name;
  private final String pidFilePath;
  private final SshShell sshShell;
  private final String statusCommand;

  /**
   * Create a new {@code RemoteProcess}.
   *
   * @param name The name of the process on the remote host
   * @param pidFilePath The path to its pidfile on the remote host
   * @param sshShell The {@code SshShell} that should be used to execute remote commands on the remote
   * @param statusCommand A bash command that, when run, will return 0 if and only if the service is running;
   *                      may include one %s which will be replaced with the service name
   */
  public RemoteProcess(String name, String pidFilePath, SshShell sshShell, String statusCommand) {
    this.name = name;
    this.pidFilePath = pidFilePath;
    this.sshShell = sshShell;
    this.statusCommand = statusCommand;
  }

  /**
   * Create a new {@code RemoteProcess} using the standard status command, {@code sudo service <name> status}.
   *
   * @param name The name of the process on the remote host
   * @param pidFilePath The path to its pidfile on the remote host
   * @param sshShell The {@code SshShell} that should be used to execute remote commands on the remote
   */
  public RemoteProcess(String name, String pidFilePath, SshShell sshShell) {
    this(name, pidFilePath, sshShell, "sudo service %s status");
  }

  private int signal(int signum) throws JSchException {
    LOG.debug("Sending signal {} to {} on {}@{}", signum, getName(), sshShell.getUsername(), sshShell.getHostname());
    return sshShell.exec(String.format("sudo kill -%d $(< %s)", signum, this.pidFilePath)).returnCode;
  }

  private int serviceCommand(String command) throws JSchException {
    LOG.debug("Sending service {} to {} on {}@{}", command, getName(), sshShell.getUsername(), sshShell.getHostname());
    return sshShell.exec(String.format("sudo service %s %s", this.name, command)).returnCode;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Starts the process using {@code service <name> start}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  public int start() throws JSchException {
    return serviceCommand("start");
  }

  /**
   * Restarts the process using {@code service <name> --full-restart}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  public int restart() throws JSchException {
    return serviceCommand("--full-restart");
  }

  /**
   * Stops the process using {@code service <name> stop}.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  public int stop() throws JSchException {
    return serviceCommand("stop");
  }

  /**
   * Terminates the process by sending it SIGTERM.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  public int terminate() throws JSchException {
    return signal(Constants.RemoteProcess.SIGTERM);
  }

  /**
   * Kills the process by sending it SIGKILL.
   *
   * @return The return code from running the process.
   * @throws JSchException
   */
  public int kill() throws JSchException {
    return signal(Constants.RemoteProcess.SIGKILL);
  }

  /**
   * Returns whether the process is running.
   *
   * @return {@code true} if running, otherwise {@code false}.
   * @throws JSchException
   */
  public boolean isRunning() throws JSchException {
    LOG.debug("Checking the status of {} on {}@{}", getName(), sshShell.getUsername(), sshShell.getHostname());
    return sshShell.exec(String.format(this.statusCommand, this.name)).returnCode == 0;
  }

  /**
   * Returns whether the process exists on a remote {@code SshShell}.
   *
   * @return {@code true} if the process exists, otherwise {@code false}
   */
  public boolean exists() throws JSchException {
    LOG.debug("Checking if {} exists", getName());
    //TODO: There should be a less sketchy method of figuring out if a service exists
    return sshShell.exec(String.format("sudo service %s 2>&1 | grep -q '%s: unrecognized service'",
                                       getName(), getName())).returnCode == 1;
  }
}
