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
import com.google.common.util.concurrent.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Main CLI frontend for ChaosMonkey.
 */
public class ChaosMonkeyCLI {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyCLI.class);

  /**
   * This class should not be able to be instantiated.
   */
  private ChaosMonkeyCLI() {}

  /**
   * Starts the ChaosMonkeyCLI service with the following parameters.
   *
   * @param processes The processes which will be managed
   * @param termFactor The probability that a process will be terminated for each execution cycle
   * @param killFactor The probability that a process will be killed for each execution cycle
   * @param executionPeriod The period (in seconds) between sucessive execution cycles.
   * @param shell The shell to use to run the commands
   * @throws IllegalStateException Thrown if service does not enter a running state after starting
   */
  private static void startChaosMonkey(Process[] processes,
                                       double termFactor,
                                       double killFactor,
                                       int executionPeriod,
                                       Shell shell) throws IllegalStateException {
    Service service = new ChaosMonkeyCLIService(processes, termFactor, killFactor, executionPeriod, shell).startAsync();
    service.awaitRunning();
    if (!service.isRunning()) {
      throw new IllegalStateException("Unable to start!");
    }
  }

  /**
   * Prints the usage guide for the CLI.
   *
   * @param runName The command which is used to invoke the CLI
   */
  private static void printHelp(String runName) {
    runName = "  " + runName;
    String usage = "\n" +
      runName + " start\n" +
      runName + " stop\n" +
      runName + " status\n" +
      runName + " help\n";
    System.out.println(usage);
  }

  /**
   * Prints the usage guide for the CLI with "<run command>" as the run name.
   */
  private static void printHelp() {
    printHelp("<run command>");
  }

  public static void main(String[] args) throws Exception {
    try {
      Configuration conf = new Configuration();
      conf.addResource("chaos-monkey-defaults.xml");
      conf.addResource("chaos-monkey-config.xml");

      double termFreq = Double.parseDouble(conf.get("termFreq"));
      double killFreq = Double.parseDouble(conf.get("killFreq"));
      int period = Integer.parseInt(conf.get("period"));

      // TODO: fix this eventually
      if (args.length != 0 && args[0].equals("help")) {
        if (args.length >= 2) {
          printHelp(args[1]);
        } else {
          printHelp();
        }
        return;
      } else if (args.length != 0) {
        LOGGER.warn("No longer need extra arguments. Configure Chaos Monkey using chaos-monkey-config.xml");
      }

      Set<Process> processList = ProcessHandler.getRunningProcesses();
      Process[] processes = processList.toArray(new Process[processList.size()]);

      startChaosMonkey(processes, termFreq, killFreq, period, new Shell());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("You must specify a command");
      printHelp();
    }
  }
}
