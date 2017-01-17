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

/**
 * A class which holds information after running a shell process.
 */
public class ShellOutput {
  /**
   * The return code of the command.
   */
  public final int returnCode;

  /**
   * The output that was captured on the standard output stream.
   */
  public final String standardOutput;

  /**
   * The output that was captured on the standard error stream.
   */
  public final String errorOutput;

  /**
   *
   * @param returnCode The return code of the command
   * @param standardOutput The standard output captured
   * @param errorOutput The standard error captured
   */
  public ShellOutput(int returnCode, String standardOutput, String errorOutput) {
    this.returnCode = returnCode;
    this.standardOutput = standardOutput;
    this.errorOutput = errorOutput;
  }
}
