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
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for Chaos Monkey Helper class
 */
public class ChaosMonkeyHelperTest {

  @Test(expected = IOException.class)
  public void testGetNodePropertiesInvalidInput() throws IOException {
    Configuration conf = new Configuration();
    conf.addResource("chaos-monkey-default.xml");
    conf.addResource("test-default.xml");
    ChaosMonkeyHelper.getNodeProperties(conf);
  }
}
