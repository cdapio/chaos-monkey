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

package io.cdap.chaosmonkey;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.cdap.chaosmonkey.proto.ClusterInfoCollector;
import io.cdap.chaosmonkey.proto.ClusterNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 *
 */
public class ConfigBasedInfoCollector implements ClusterInfoCollector {
  private Collection<ClusterNode> nodes;

  @Override
  public void initialize(Map<String, String> properties) throws Exception {
    Multimap<String, String> hostToServices = HashMultimap.create();
    Collection<String> services = Arrays.asList(properties.get("services").split(","));
    for (String service : services) {
      Collection<String> hosts = Arrays.asList(properties.get(service + ".hosts").split(","));
      for (String host : hosts) {
        hostToServices.put(host, service);
      }
    }

    nodes = new HashSet<>();
    for (String host : hostToServices.keySet()) {
      nodes.add(new ClusterNode(hostToServices.get(host), host));
    }
  }

  @Override
  public Collection<ClusterNode> getNodeProperties() throws Exception {
    return nodes;
  }
}
