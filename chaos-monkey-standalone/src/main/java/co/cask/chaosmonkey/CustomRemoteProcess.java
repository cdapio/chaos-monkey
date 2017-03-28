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
import com.google.common.collect.ImmutableMap;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A remote process that ScheduledDisruption can interact with.
 */
public class CustomRemoteProcess extends SysVRemoteProcess {
  private static final Logger LOG = LoggerFactory.getLogger(CustomRemoteProcess.class);

  private final ImmutableMap<String, String> customCommands;

  /**
   * Create a new {@code RemoteProcess}.
   *
   * @param name The name of the process on the remote host
   * @param pidFilePath The path to its pidfile on the remote host
   * @param sshShell The {@code SshShell} that should be used to execute remote commands on the remote
   * @param customCommands An override of the default commands
   */
  public CustomRemoteProcess(String name, String pidFilePath, SshShell sshShell,
                             ImmutableMap<String, String> customCommands) {
    super(name, pidFilePath, sshShell);
    this.customCommands = customCommands;
  }

  @Override
  public int start() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.START)) {
      return execAndGetReturnCode(customCommands.get(Constants.RemoteProcess.START));
    } else {
      return super.start();
    }
  }

  @Override
  public int restart() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.RESTART)) {
      return execAndGetReturnCode(customCommands.get(Constants.RemoteProcess.RESTART));
    } else {
      return super.restart();
    }
  }

  @Override
  public int stop() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.STOP)) {
      return execAndGetReturnCode(customCommands.get(Constants.RemoteProcess.STOP));
    } else {
      return super.stop();
    }
  }

  @Override
  public int terminate() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.TERMINATE)) {
      return execAndGetReturnCode(customCommands.get(Constants.RemoteProcess.TERMINATE));
    } else {
      return super.terminate();
    }
  }

  @Override
  public int kill() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.KILL)) {
      return execAndGetReturnCode(customCommands.get(Constants.RemoteProcess.KILL));
    } else {
      return super.kill();
    }
  }

  @Override
  public boolean isRunning() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.IS_RUNNING)) {
      return execAndReturnSucessful(customCommands.get(Constants.RemoteProcess.IS_RUNNING));
    } else {
      return super.isRunning();
    }
  }

  @Override
  public boolean exists() throws JSchException {
    if (customCommands.containsKey(Constants.RemoteProcess.EXISTS)) {
      return execAndReturnSucessful(customCommands.get(Constants.RemoteProcess.EXISTS));
    } else {
      return super.exists();
    }
  }
}
