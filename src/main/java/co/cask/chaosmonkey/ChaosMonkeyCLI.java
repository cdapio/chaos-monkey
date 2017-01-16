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

import com.google.common.util.concurrent.Service;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Main CLI frontend for ChaosMonkey.
 */
public class ChaosMonkeyCLI {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyCLI.class);

  static {
    Options options = new Options();

    Option[] optionArray = new Option[] {
      Option.builder("p")
        .argName("period")
        .desc("attempts to kill every given number of seconds")
        .longOpt("period")
        .numberOfArgs(1)
        .valueSeparator(' ')
        .build(),
      Option.builder("s")
        .argName("shell")
        .desc("execute with the given shell values where %s is overwritten (one argument per shell value)")
        .longOpt("shell")
        .numberOfArgs(1)
        .valueSeparator(' ')
        .build(),
    };

    for (Option option : optionArray) {
      options.addOption(option);
    }

    cliOptions = options;
  }

  private static final Options cliOptions;
  private static final int REQUIRED_ARGUMENTS = 2;

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
      runName + " start [options] <termFrequency> <killFrequency> [<PID paths>...]\n" +
      runName + " stop\n" +
      runName + " status\n" +
      runName + " help\n" +
      "\n" +
      "Options:\n";
    new HelpFormatter().printHelp(usage, cliOptions);
  }

  /**
   * Prints the usage guide for the CLI with "<run command>" as the run name.
   */
  private static void printHelp() {
    printHelp("<run command>");
  }

  public static void main(String[] args) throws Exception {
    try {
      if (args.length == 0) {
        throw new ParseException("You must specify a command");
      } else if (args[0].equals("help")) {
        if (args.length >= 2) {
          printHelp(args[1]);
        } else {
          printHelp();
        }
        return;
      }

      CommandLine cl = new DefaultParser().parse(cliOptions, args);
      String[] leftovers = cl.getArgs();

      if (leftovers.length < REQUIRED_ARGUMENTS) {
        throw new ParseException("Not enough arguments");
      }

      Process[] processes;
      if (leftovers.length == REQUIRED_ARGUMENTS) {
        Set<Process> processList = ProcessHandler.getRunningProcesses();
        processes = processList.toArray(new Process[processList.size()]);
      } else {
        processes = new Process[leftovers.length - REQUIRED_ARGUMENTS];
        for (int i = 0; i < leftovers.length - REQUIRED_ARGUMENTS; i++) {
          processes[i] = new Process(leftovers[i + REQUIRED_ARGUMENTS]);
        }
      }

      double termFreq = Double.parseDouble(leftovers[0]);
      double killFreq = Double.parseDouble(leftovers[1]);
      int period = Integer.parseInt(cl.getOptionValue("period", "1"));

      Shell shell;
      if (cl.hasOption("shell")) {
        int overwriteIndex = -1;
        String[] shellArgs = cl.getOptionValues("shell");
        for (int i = 0; i < shellArgs.length; i++) {
          if (shellArgs[i].equals("%s")) {
            shellArgs[i] = null;
            overwriteIndex = i;
            break;
          }
        }
        shell = new Shell(shellArgs, overwriteIndex);
      } else {
        shell = new Shell();
      }

      startChaosMonkey(processes, termFreq, killFreq, period, shell);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.out.println("You must specify a command");
      printHelp();
    } catch (ParseException e) {
      System.out.println(e.getMessage());
      printHelp();
    }
  }
}
