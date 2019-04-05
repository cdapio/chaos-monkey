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

package io.cdap.chaosmonkey.proto;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * NodeStatus represents the running status of each service on a node
 */
public class NodeStatus {
  private final String hostname;
  private final Map<String, String> serviceStatusMap;

  public NodeStatus(String hostname, Map<String, String> serviceStatusMap) {
    this.hostname = hostname;
    this.serviceStatusMap = serviceStatusMap;
  }

  public NodeStatus(String hostname, Collection<ServiceStatus> serviceStatuses) {
    this.hostname = hostname;
    Map<String, String> serviceStatusMap = new HashMap<>();
    for (ServiceStatus serviceStatus : serviceStatuses) {
      if (!serviceStatus.getAddress().equals(hostname)) {
        throw new RuntimeException("Service status does not match given hostname");
      }
      serviceStatusMap.put(serviceStatus.getService(), serviceStatus.getStatus());
    }
    this.serviceStatusMap = serviceStatusMap;
  }

  public String getHostname() {
    return hostname;
  }

  public Map<String, String> getServiceStatusMap() {
    return Collections.unmodifiableMap(this.serviceStatusMap);
  }
}
