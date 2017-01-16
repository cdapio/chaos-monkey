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

/**
 * TODO: documentations later
 */
public class ConfigurationTest {

  // TODO: unit test this later
  public static void main (String[] args) {
    Configuration conf = new Configuration();
    conf.addResource("chaos-monkey-defaults.xml");

    String test = conf.get("test.ip");
    System.out.println(test);
    test = conf.get("test.name");
    System.out.println(test);
  }
}
