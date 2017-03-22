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

import co.cask.chaosmonkey.proto.ClusterNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * NodeProperties represents the JSON object returned by coopr
 */
public class CooprNodeProperties extends ClusterNode {
  private final Map<String, String> ipaddresses;

  public CooprNodeProperties(Collection<String> services, String ipAddress) {
    super(services, ipAddress);
    Map<String, String> ipAddresses = new HashMap<>();
    ipAddresses.put("access_v4", ipAddress);

    this.ipaddresses = ipAddresses;
  }

  @Override
  public String getHost() {
    return ipaddresses.get("access_v4");
  }
}
