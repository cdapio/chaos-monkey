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

import co.cask.chaosmonkey.proto.Action;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote Process to represent CDAP master
 */
public class CdapMasterRemoteProcess extends SysVRemoteProcess {
  private static final Logger LOG = LoggerFactory.getLogger(CdapMasterRemoteProcess.class);

  public CdapMasterRemoteProcess(String name, SshShell sshShell) {
    super(name, null, sshShell);
  }

  private int serviceCommand(String command) throws JSchException {
    LOG.debug("Sending service {} to {} on {}@{}", command, getName(), sshShell.getUsername(), sshShell.getAddress());
    return execAndGetReturnCode(String.format("sudo cdap master %s", command));
  }

  /**
   * Starts CDAP master using {@code cdap master start}
   *
   * @return The return code from exeuting the command
   * @throws JSchException
   */
  @Override
  public int start() throws JSchException {
    return serviceCommand(Action.START.getCommand());
  }

  /**
   * Restarts CDAP master using {@code cdap master restart}
   *
   * @return The return code from executing the command
   * @throws JSchException
   */
  @Override
  public int restart() throws JSchException {
    return serviceCommand(Action.RESTART.getCommand());
  }

  /**
   * Stops CDAP master using {@code cdap master stop}
   *
   * @return The return code from executing the command
   * @throws JSchException
   */
  @Override
  public int stop() throws JSchException {
    return serviceCommand(Action.STOP.getCommand());
  }

  /**
   * Equivalent of using stop command
   *
   * @return The return code from executing the command
   * @throws JSchException
   */
  @Override
  public int terminate() throws JSchException {
    return serviceCommand(Action.STOP.getCommand());
  }

  /**
   * Equivalent of using stop command
   *
   * @return The return code from executing the command
   * @throws JSchException
   */
  @Override
  public int kill() throws JSchException {
    return serviceCommand(Action.STOP.getCommand());
  }

  /**
   * Returns whether CDAP master is running
   *
   * @return {@code true} if running, otherwise {@code false}
   * @throws JSchException
   */
  @Override
  public boolean isRunning() throws JSchException {
    LOG.debug("Checking the status of {} on {}@{}", getName(), sshShell.getUsername(), sshShell.getAddress());
    return execAndReturnSucessful("sudo cdap master status");
  }
}
