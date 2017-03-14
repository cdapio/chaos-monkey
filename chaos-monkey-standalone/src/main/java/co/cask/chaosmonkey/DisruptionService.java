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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service to keep track of running disruptions
 */
public class DisruptionService {

  private Table<String, String, AtomicBoolean> status;

  public DisruptionService() {
    status = HashBasedTable.create();
  }

  public boolean checkRunning(String service, String action) {
    if (status.get(service, action) == null) {
      return false;
    }
    return status.get(service, action).get();
  }

  public boolean checkAndStart(String service, String action) {
    if (status.get(service, action) != null && status.get(service, action).get()) {
      return false;
    }

    status.put(service, action, new AtomicBoolean(true));
    return true;
  }

  public void release(String service, String action) {
    status.put(service, action, new AtomicBoolean(false));
  }
}
