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

import co.cask.chaosmonkey.common.Constants;
import com.jcraft.jsch.JSchException;

/**
 * A halting disruption that kills the process
 */
public class Kill extends AbstractHaltingDisruption {

  @Override
  public String getName() {
    return "kill";
  }

  @Override
  protected void action(RemoteProcess process) throws JSchException {
    process.execAndGetReturnCode(String.format("sudo kill -%d $(< %s)", Constants.RemoteProcess.SIGKILL,
                                               process.getPidFile()));
  }
}
