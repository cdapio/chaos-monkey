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

import co.cask.chaosmonkey.conf.Configuration;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.jcraft.jsch.JSchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The main service that will be running ChaosMonkey.
 */
public class ChaosMonkeyService extends AbstractScheduledService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyService.class);

  private RemoteProcess process;
  private double termFactor;
  private double killFactor;
  private int executionPeriod;

  /**
   *
   * @param process The processes that will be managed
   * @param termFactor The probability that a process will be terminated on each execution cycle
   * @param killFactor The probability that a process will be killed on each execution cycle
   * @param executionPeriod The rate of execution cycles (in seconds)
   */
  public ChaosMonkeyService(RemoteProcess process,
                            double termFactor,
                            double killFactor,
                            int executionPeriod) {
    this.process = process;
    this.termFactor = termFactor;
    this.killFactor = killFactor;
    this.executionPeriod = executionPeriod;
  }

  @Override
  protected void runOneIteration() throws Exception {
    if (Math.random() < killFactor) {
      process.kill();
    } else if (Math.random() < termFactor) {
      process.terminate();
    }
  }

  @Override
  protected Scheduler scheduler() {
    return AbstractScheduledService.Scheduler.newFixedRateSchedule(0, this.executionPeriod, TimeUnit.SECONDS);
  }

  public static void main(String[] args) throws JSchException {
    Configuration conf = new Configuration();
    conf.addResource("chaos-monkey-default.xml");
    conf.addResource("chaos-monkey-site.xml");

    String username = conf.get("username", System.getProperty("user.name"));
    String privateKey = conf.get("privateKey");
    String keyPassphrase = conf.get("keyPassphrase");

    // TODO: can be replaced with a better way to get hostnames
    String[] hostnames;
    try {
      hostnames = conf.get("hostnames").split(",");
    } catch (NullPointerException e) {
      throw new IllegalArgumentException("You must provide a list of comma-separated hostnames", e);
    }

    SshShell[] sshShells = new SshShell[hostnames.length];
    for (int i = 0; i < hostnames.length; i++) {
      if (privateKey != null) {
        if (keyPassphrase != null) {
          sshShells[i] = new SshShell(username, hostnames[i], privateKey);
        } else {
          sshShells[i] = new SshShell(username, hostnames[i], privateKey, keyPassphrase);
        }
      } else {
        sshShells[i] = new SshShell(username, hostnames[i]);
      }
    }

    Set<ChaosMonkeyService> services = new HashSet<>();
    for (String service : conf.get("services").split(",")) {
      for (SshShell sshShell : sshShells) {
        String pidPath;
        int interval;
        double killProbability;
        double stopProbability;

        pidPath = conf.get(service + ".pidPath");
        if (pidPath == null) {
          throw new IllegalArgumentException("The following process does not have a pidPath: " + service);
        }

        try {
          interval = Integer.parseInt(conf.get(service + ".interval"));
        } catch (NumberFormatException | NullPointerException e) {
          throw new IllegalArgumentException("The following process does not have a valid interval: " + service, e);
        }

        killProbability = Double.parseDouble(conf.get(service + ".killProbability", "0.0"));
        stopProbability = Double.parseDouble(conf.get(service + ".stopProbability", "0.0"));

        if (killProbability == 0.0 && stopProbability == 0.0) {
          throw new IllegalArgumentException("The following process may not have both killProbability and " +
                                               "stopProbability equal to 0.0 or undefined: " + service);
        }

        RemoteProcess process = new RemoteProcess(service, pidPath, sshShell);
        ChaosMonkeyService chaosMonkeyService = new ChaosMonkeyService(process, stopProbability,
                                                                       killProbability, interval);
        services.add(chaosMonkeyService);
      }
    }

    if (services.isEmpty()) {
      throw new IllegalArgumentException("No processes specified in configs");
    }

    for (ChaosMonkeyService service : services) {
      service.awaitRunning();
    }
  }
}
