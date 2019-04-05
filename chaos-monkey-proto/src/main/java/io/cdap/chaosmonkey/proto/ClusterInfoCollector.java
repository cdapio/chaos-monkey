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
import java.util.Map;

/**
 * ClusterInfoCollector allows for different ways to collect location and services of nodes in a cluster
 */
public interface ClusterInfoCollector {

  /**
   * Initialize the cluster info collector with given configurations
   *
   * @param properties configurations for cluster info collector
   * @throws Exception
   */
  void initialize(Map<String, String> properties) throws Exception;

  /**
   * Gathers the location and services of each node to be used by Chaos Monkey
   *
   * @return Collection of {@link ClusterNode}
   * @throws Exception
   */
  Collection<ClusterNode> getNodeProperties() throws Exception;
}
