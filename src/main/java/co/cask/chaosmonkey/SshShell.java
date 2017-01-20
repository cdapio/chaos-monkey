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

import com.google.common.collect.ImmutableList;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class SSH access to remote hosts.
 */
public class SshShell {

  private static final ImmutableList<String> RELATIVE_KEY_PATHS = ImmutableList.of(".ssh/id_dsa",
                                                                                   ".ssh/id_ecdsa",
                                                                                   ".ssh/id_rsa");

  private final JSch jsch;
  private final String username;
  private final String hostname;

  public SshShell(String username, String hostname, String privateKey, String passphrase) throws JSchException {
    this.username = username;
    this.hostname = hostname;

    this.jsch = new JSch();
    jsch.setConfig("StrictHostKeyChecking", "no");

    if (privateKey != null) {
      if (passphrase != null) {
        jsch.addIdentity(privateKey, passphrase);
      } else {
        jsch.addIdentity(privateKey);
      }
    }
  }

  public SshShell(String username, String hostname, String privateKey) throws JSchException {
    this(username, hostname, privateKey, null);
  }

  public SshShell(String username, String hostname) throws JSchException {
    this(username, hostname, null);

    for (String relativeKeyPath : RELATIVE_KEY_PATHS) {
      String absoluteKeyPath = System.getProperty("user.home") + "/" + relativeKeyPath;
      if (new File(absoluteKeyPath).exists()) {
        jsch.addIdentity(absoluteKeyPath);
        break;
      }
    }
  }

  public ShellOutput exec(String command, InputStream input) throws JSchException {
    Session session = jsch.getSession(this.username, hostname);
    try {
      session.connect();
      ChannelExec channel = (ChannelExec) session.openChannel("exec");

      try (ByteArrayOutputStream output = new ByteArrayOutputStream();
          ByteArrayOutputStream error = new ByteArrayOutputStream()) {
        channel.setCommand(command);
        channel.setInputStream(input);
        channel.setOutputStream(output);
        channel.setErrStream(error);
        channel.connect();

        while (channel.getExitStatus() < 0) {
          // wait for status to become >= 0
        }
        return new ShellOutput(channel.getExitStatus(), output.toString(), error.toString());
      } catch (IOException e) {
        // Execution should never reach here because ByteArayOutputStream should never throw this exception
        throw new IllegalStateException("This should not happen", e);
      } finally {
        channel.disconnect();
      }

    } finally {
      session.disconnect();
    }
  }

  public ShellOutput exec(String command) throws JSchException {
    return exec(command, null);
  }
}
