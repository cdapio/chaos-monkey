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

import java.util.Collection;

/**
 * A disruption that alters the status of running processes
 */
public interface Disruption {

  /**
   * Perform the disruption on the given list of processes
   * @param processes Collection of processes to disrupt
   */
  void disrupt(Collection<RemoteProcess> processes) throws Exception;

  /**
   * Get the name of this disruption
   * @return
   */
  String getName();
}
