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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * TODO: Fill this out later
 */
public class ShellTest {

  private static Shell shell;

  @BeforeClass
  public static void setup() {
    shell = new Shell();
  }

  @Test
  public void testConstructor() throws IOException {
    String[] echoRunner = {"echo", null};
    Shell echoShell = new Shell(echoRunner);
    ShellOutput output = echoShell.exec("what's up world?");

    Assert.assertEquals(0, output.returnCode);
    Assert.assertEquals("what's up world?\n", output.standardOutput);
    Assert.assertEquals("", output.errorOutput);
  }

  @Test
  public void testSimple() throws IOException {
    ShellOutput output = shell.exec("echo hello world");

    Assert.assertEquals(0, output.returnCode);
    Assert.assertEquals("hello world\n", output.standardOutput);
    Assert.assertEquals("", output.errorOutput);
  }

  @Test
  public void testCommandNotFound() throws IOException {
    ShellOutput output = shell.exec("thisisnotacommand");

    Assert.assertEquals(127, output.returnCode);
    Assert.assertEquals("", output.standardOutput);
    Assert.assertEquals("bash: thisisnotacommand: command not found\n", output.errorOutput);
  }

  @Test
  public void testPipe() throws IOException {
    ShellOutput output = shell.exec("echo hello world | sed 's/hello/what is up/'");

    Assert.assertEquals(0, output.returnCode);
    Assert.assertEquals("what is up world\n", output.standardOutput);
    Assert.assertEquals("", output.errorOutput);
  }

  @Test
  public void testMultiple() throws IOException {
    ShellOutput output = shell.exec("echo hello world");
    Assert.assertEquals(0, output.returnCode);
    Assert.assertEquals("hello world\n", output.standardOutput);
    Assert.assertEquals("", output.errorOutput);

    output = shell.exec("thisshoulderror");
    Assert.assertEquals(127, output.returnCode);
    Assert.assertEquals("", output.standardOutput);
    Assert.assertEquals("bash: thisshoulderror: command not found\n", output.errorOutput);

    output = shell.exec("echo we all good");
    Assert.assertEquals(0, output.returnCode);
    Assert.assertEquals("we all good\n", output.standardOutput);
    Assert.assertEquals("", output.errorOutput);
  }
}
