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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * TODO: Fill this out
 */
public class Shell {

  public static final String[] BASH_RUNNER = {
    "bash",
    "-c",
    null
  };

  private final String[] shellRunner;
  private final int lastIndex;

  /**
   * Constructs a new {@code Shell} with {@code shellRunner} as the runner.
   *
   * @param shellRunner The command that will be passed to {@code Runtime.exec()}, where the last element should be
   * {@code null} and will be overwritten when {@code exec} is called.
   */
  public Shell(String[] shellRunner) {
    this.lastIndex = shellRunner.length - 1;
    this.shellRunner = shellRunner.clone();

    if (this.shellRunner[this.lastIndex] != null) {
      throw new IllegalArgumentException("Last element of shellRunner should be null");
    }
  }

  /**
   * Constructs a new {@code Shell} with {@code bash} as the runner.
   */
  public Shell() {
    this(BASH_RUNNER);
  }

  public static String readReader(Reader reader) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    int nextInt;

    while ((nextInt = reader.read()) > 0) {
      stringBuilder.append((char) nextInt);
    }

    return stringBuilder.toString();
  }

  public ShellOutput exec(String command) throws IOException {
    this.shellRunner[lastIndex] = command;
    Process process = Runtime.getRuntime().exec(this.shellRunner);

    try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));) {

      int returnCode = process.waitFor();
      String outputString = readReader(outputReader);
      String errorString = readReader(errorReader);

      return new ShellOutput(returnCode, outputString, errorString);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }
}
