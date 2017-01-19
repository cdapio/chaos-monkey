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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Main CLI frontend for ChaosMonkey.
 */
public class ChaosMonkeyCLI {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyCLI.class);
  private static Set<Service> services = new HashSet<>();

  /**
   * This class should not be able to be instantiated.
   */
  private ChaosMonkeyCLI() {}

  /**
   * Starts the ChaosMonkeyCLI service with the following parameters.
   *
   * @param processRules The processes which will be managed
   * @param shell The shell to use to run the commands
   * @throws IllegalStateException Thrown if service does not enter a running state after starting
   */
  private static void startChaosMonkey(ProcessRule[] processRules,
                                       Shell shell) throws IllegalStateException {
    for (ProcessRule processRule : processRules) {
      Service service = new ChaosMonkeyCLIService(processRule.getProcess(),
                                                  processRule.getStopProbability(),
                                                  processRule.getKillProbability(),
                                                  processRule.getInterval(),
                                                  shell).startAsync();
      service.awaitRunning();
      if (service.isRunning()) {
        services.add(service);
      }
    }

    if (services.size() == 0) {
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

      Configuration conf = new Configuration();
      conf.addResource("chaos-monkey-default.xml");
      conf.addResource("chaos-monkey-site.xml");

      Set<ProcessRule> processList = new HashSet<>();
      Field[] fields = Constants.Process.class.getFields();
      for (Field field : fields) {
        field.setAccessible(true);
        String processName = (String) field.get(null);

        int interval;
        double killProbability = 0;
        double stopProbability = 0;

        if (conf.get(processName + ".interval") == null) {
          LOGGER.info(processName + " interval not specified. Chaos monkey will spare this process.");
          continue;
        }
        interval = Integer.parseInt(conf.get(processName + ".interval"));

        if (conf.get(processName + ".killProbability") != null) {
          killProbability = Double.parseDouble(conf.get(processName + ".killProbability"));
        }
        if (conf.get(processName + ".stopProbability") != null) {
          stopProbability = Double.parseDouble(conf.get(processName + ".stopProbability"));
        }

        if (killProbability == 0 && stopProbability == 0) {
          LOGGER.info(processName + " stop/kill probability not specified. Chaos monkey will spare this process.");
          continue;
        }
        Process process = Process.PROCESS_MAP.get(processName);
        ProcessRule processRule = new ProcessRule(process, killProbability, stopProbability, 0, interval);

        processList.add(processRule);
      }
      if (processList.isEmpty()) {
        throw new IllegalStateException("No process specified in configs");
      }

      ProcessRule[] processes = processList.toArray(new ProcessRule[processList.size()]);
      startChaosMonkey(processes, new Shell());
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("You must specify a command");
      printHelp();
    }
  }
}
