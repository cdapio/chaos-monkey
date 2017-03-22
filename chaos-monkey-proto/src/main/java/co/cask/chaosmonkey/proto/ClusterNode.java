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

package co.cask.chaosmonkey.proto;

import java.util.Collection;

/**
 * Represents a node, with information to be returned from {@link ClusterInfoCollector}
 */
public class ClusterNode {
  private final Collection<String> services;
  private final String host;

  public ClusterNode(Collection<String> services, String host) {
    this.services = services;
    this.host = host;
  }

  public Collection<String> getServices() {
    return services;
  }

  public String getHost() {
    return host;
  }
}
