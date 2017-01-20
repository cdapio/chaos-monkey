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

  public static void main(String[] args) {
    Configuration conf = new Configuration();
    conf.addResource("chaos-monkey-default.xml");
    conf.addResource("chaos-monkey-site.xml");

    SshShell[] sshShells = {null, null, null}; // TODO: This will be replaced with a way to actually get remote hosts
    Set<ChaosMonkeyService> services = new HashSet<>();
    for (SshShell sshShell : sshShells) {
      for (String service : conf.get("services").split(",")) {
        String pidPath;
        int interval;
        double killProbability = 0;
        double stopProbability = 0;

        if (conf.get(service + ".pidPath") == null) {
          LOGGER.info(service + " path of PID file not specified. Chaos monkey will spare this process.");
          continue;
        }
        pidPath = conf.get(service + ".pidPath");

        if (conf.get(service + ".interval") == null) {
          LOGGER.info(service + " interval not specified. Chaos monkey will spare this process.");
          continue;
        }
        interval = Integer.parseInt(conf.get(service + ".interval"));

        if (conf.get(service + ".killProbability") != null) {
          killProbability = Double.parseDouble(conf.get(service + ".killProbability"));
        }
        if (conf.get(service + ".stopProbability") != null) {
          stopProbability = Double.parseDouble(conf.get(service + ".stopProbability"));
        }

        if (killProbability == 0 && stopProbability == 0) {
          LOGGER.info(service + " stop/kill probability not specified. Chaos monkey will spare this process.");
          continue;
        }
        RemoteProcess process = new RemoteProcess(service, pidPath, sshShell);
        ChaosMonkeyService chaosMonkeyService = new ChaosMonkeyService(process, stopProbability,
                                                                       killProbability, interval);
        services.add(chaosMonkeyService);
      }
    }

    if (services.isEmpty()) {
      throw new IllegalStateException("No process specified in configs");
    }

    for (ChaosMonkeyService service : services) {
      service.awaitRunning();
    }
  }
}
