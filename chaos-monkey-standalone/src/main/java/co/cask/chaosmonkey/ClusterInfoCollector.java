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

import co.cask.chaosmonkey.common.conf.Configuration;
import co.cask.chaosmonkey.proto.NodeProperties;

import java.util.Collection;

/**
 * ClusterInfoCollector allows for different ways to collect location and services of nodes in a cluster
 */
public interface ClusterInfoCollector {

  /**
   * Gathers the location and services of each node to be used by Chaos Monkey
   *
   * @param conf instance of {@link Configuration}
   * @return Collection of {@link NodeProperties}
   * @throws Exception
   */
  Collection<NodeProperties> getNodeProperties(Configuration conf) throws Exception;
}
