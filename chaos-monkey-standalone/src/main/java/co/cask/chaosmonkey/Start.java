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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A disruption that starts a process
 */
public class Start implements Disruption {
  private static final Logger LOGGER = LoggerFactory.getLogger(Start.class);

  @Override
  public void disrupt(Collection<RemoteProcess> processes, @Nullable Map<String, String> serviceArguments) throws Exception {
    for (RemoteProcess process : processes) {
      if (!process.isRunning()) {
        LOGGER.info("Attempting to {} {} on {}", this.getName(), process.getName(), process.getAddress());
        process.execAndGetReturnCode(String.format("sudo service %s %s", process.getName(), this.getName()));

        if (process.isRunning()) {
          LOGGER.info("{} on {} is now running", process.getName(), process.getAddress());
        } else {
          LOGGER.error("{} on {} is still down after start attempt!", process.getName(), process.getAddress());
        }
      } else {
        LOGGER.info("{} on {} is already running, skipping {} attempt", process.getName(), process.getAddress(),
                    this.getName());
      }
    }
  }

  @Override
  public String getName() {
    return "start";
  }
}
