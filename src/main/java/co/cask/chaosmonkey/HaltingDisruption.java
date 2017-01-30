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

import java.util.List;

/**
 * A disruption that halts a running process
 */
public abstract class HaltingDisruption implements Disruption {
  private static final Logger LOGGER = LoggerFactory.getLogger(HaltingDisruption.class);

  @Override
  public void disrupt(List<RemoteProcess> processes) throws Exception {
    for (RemoteProcess process : processes) {
      if (process.isRunning()) {
        LOGGER.info("Attempting to {} {} on {}", this.getName(), process.getName(), process.getAddress());
        this.action(process);

        if (process.isRunning()) {
          LOGGER.error("{} on {} is still running!", process.getName(), process.getAddress());
        } else {
          LOGGER.info("{} on {} is no longer running", process.getName(), process.getAddress());
        }
      } else {
        LOGGER.info("{} on {} is not running, skipping {} attempt", process.getName(), process.getAddress(),
                    this.getName());
      }
    }
  }

  protected abstract void action(RemoteProcess process) throws Exception;
}
