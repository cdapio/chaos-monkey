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

  public RemoteProcess(String name, String pidFilePath, SshShell sshShell) {
    this.name = name;
    this.pidFilePath = pidFilePath;
    this.sshShell = sshShell;
  }

  private int signal(int signum) throws JSchException {
    return sshShell.exec(String.format("kill -%d $(cat %s)", signum, this.pidFilePath)).returnCode;
  }

  private int serviceCommand(String command) throws JSchException {
    return sshShell.exec(String.format("sudo service %s %s", this.name, command)).returnCode;
  }

  public int start() throws JSchException {
    return serviceCommand("start");
  }

  public int restart() throws JSchException {
    return serviceCommand("--full-restart");
  }

  public int stop() throws JSchException {
    return serviceCommand("stop");
  }

  public int terminate() throws JSchException {
    return signal(Constants.Process.SIGTERM);
  }

  public int kill() throws JSchException {
    return signal(Constants.Process.SIGKILL);
  }

  public boolean isRunning() throws JSchException {
    return sshShell.exec(String.format("service %s status | grep OK", this.name)).returnCode == 0;
  }
}
