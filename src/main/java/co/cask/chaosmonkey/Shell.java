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

import com.google.common.io.CharStreams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 *
 */
public class Shell {

  private final String[] shellRunner;
  private final int overwriteIndex;

  /**
   * Constructs a new {@code Shell} with {@code shellRunner} as the runner.
   *
   * @param shellRunner The command that will be passed to {@code Runtime.exec()}, where the last element should be
   * {@code null} and will be overwritten when {@code exec} is called.
   */
  public Shell(String[] shellRunner, int overwriteIndex) {
    this.overwriteIndex = overwriteIndex;
    this.shellRunner = shellRunner.clone();

    if (this.shellRunner[this.overwriteIndex] != null) {
      throw new IllegalArgumentException("The overwriteIndex should have a null element");
    }
  }

  /**
   * Constructs a new {@code Shell} with {@code bash} as the runner.
   */
  public Shell() {
    this(new String[] { "bash", "-c", null }, 2);
  }

  /**
   * Execute the command given.
   *
   * @param command The command which will be run.
   * @return The output of the command
   * @throws IOException
   */
  public ShellOutput exec(String command) throws IOException {
    this.shellRunner[overwriteIndex] = command;
    java.lang.Process process = Runtime.getRuntime().exec(this.shellRunner);

    try (BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

      int returnCode = process.waitFor();
      String outputString = CharStreams.toString(outputReader);
      String errorString = CharStreams.toString(errorReader);

      return new ShellOutput(returnCode, outputString, errorString);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }
}
