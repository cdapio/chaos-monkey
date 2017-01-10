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

/**
 * TODO: procrastinate on documentation
 */
public class CommandServiceTest {

  private static CommandService commandService;

  @BeforeClass
  public static void setup() {
    Shell shell = new Shell();
    commandService = new CommandService(shell);
  }

  // TODO: This test currently works because HBase isn't running, but we would need a different test in the future
  @Test
  public void testKillNonExistentPath() {
    try {
      commandService.killProcess(Service.HBaseMaster);
      Assert.fail();
    } catch (Exception e) {
      Assert.assertEquals(e.getMessage(), "Process ID not found");
    }
  }
}
