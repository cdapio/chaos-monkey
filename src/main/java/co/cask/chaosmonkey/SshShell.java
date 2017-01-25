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
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class allows SSH access to remote hosts.
 */
public class SshShell {
  private static final Logger LOG = LoggerFactory.getLogger(SshShell.class);
  private static final ImmutableList<String> RELATIVE_KEY_PATHS = ImmutableList.of(".ssh/id_dsa",
                                                                                   ".ssh/id_ecdsa",
                                                                                   ".ssh/id_rsa");

  private final JSch jsch;
  private final String username;
  private final NodeProperties nodeProperties;

  /**
   * Constructs a new {@code SshShell}.
   *
   * @param username The username to connect with
   * @param nodeProperties Holds information about the node to connect to
   * @param privateKey The location of the private key file
   * @param passphrase The passphrase encrypting the private key
   * @throws JSchException
   */
  public SshShell(String username, NodeProperties nodeProperties,
                  String privateKey, String passphrase) throws JSchException {
    this.username = username;
    this.nodeProperties = nodeProperties;

    this.jsch = new JSch();
    this.jsch.setConfig("StrictHostKeyChecking", "no");

    try {
      Connector connector = ConnectorFactory.getDefault().createConnector();
      if (connector != null) {
        jsch.setIdentityRepository(new RemoteIdentityRepository(connector));
        LOG.debug("Attaching to ssh-agent");
      }
    } catch (AgentProxyException e) {
      LOG.error("Unable to connect to ssh-agent", e);
    }

    if (privateKey != null) {
      if (passphrase != null) {
        jsch.addIdentity(privateKey, passphrase);
      } else {
        jsch.addIdentity(privateKey);
      }
    }
  }

  /**
   * Constructs a new {@code SshShell} where the private key is unencrypted.
   *
   * @param username The username to connect with
   * @param nodeProperties Holds information about the node to connect to
   * @param privateKey The location of the private key file
   * @throws JSchException
   */
  public SshShell(String username, NodeProperties nodeProperties, String privateKey) throws JSchException {
    this(username, nodeProperties, privateKey, null);
  }

  /**
   * Constructs a new {@code SshShell} by looking in the default key locations; keys should be unencrypted.
   *
   * @param username The username to connect with
   * @param nodeProperties Holds information about the node to connect to
   * @throws JSchException
   */
  public SshShell(String username, NodeProperties nodeProperties) throws JSchException {
    this(username, nodeProperties, null);

    boolean noIdentity = true;
    for (String relativeKeyPath : RELATIVE_KEY_PATHS) {
      String absoluteKeyPath = System.getProperty("user.home") + "/" + relativeKeyPath;
      if (new File(absoluteKeyPath).exists()) {
        jsch.addIdentity(absoluteKeyPath);
        noIdentity = false;
        break;
      }
    }

    if (noIdentity) {
      throw new IllegalStateException("No keys found, please manually add your key");
    }
  }

  /**
   * Execute a command on a remote host.
   *
   * @param command The command to be executed
   * @param input The input to accompany the command
   * @return The output of the command
   * @throws JSchException
   */
  public ShellOutput exec(String command, InputStream input) throws JSchException {
    Session session = jsch.getSession(this.username, this.getNodeProperties().getAccessIpAddress());
    command = String.format("bash -lc '%s'", command);
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
        LOG.debug("Executing '{}' to {}@{}", command, getUsername(), this.getNodeProperties().getAccessIpAddress());

        while (channel.getExitStatus() < 0) {
          try {
            Thread.sleep(250);
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
        return new ShellOutput(channel.getExitStatus(), output.toString(), error.toString());
      } catch (IOException e) {
        // Execution should never reach here because ByteArrayOutputStream should never throw this exception
        throw new IllegalStateException("This should not happen", e);
      } finally {
        channel.disconnect();
      }

    } finally {
      session.disconnect();
    }
  }

  public String getUsername() {
    return this.username;
  }

  public String getHostname() {
    return this.nodeProperties.getHostname();
  }

  public NodeProperties getNodeProperties() {
    return this.nodeProperties;
  }

  /**
   * Execute a command on a remote host.
   *
   * @param command The command to be executed
   * @return The output of the command
   * @throws JSchException
   */
  public ShellOutput exec(String command) throws JSchException {
    return exec(command, null);
  }
}
