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

import co.cask.chaosmonkey.common.Constants;
import co.cask.chaosmonkey.common.conf.Configuration;
import co.cask.chaosmonkey.proto.ClusterInfoCollector;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for interacting with clusters
 */
public class Clusters {

  public static ClusterInfoCollector createInitializedInfoCollector(Configuration conf) throws Exception {
    ClusterInfoCollector clusterInfoCollector = Class.forName(
      conf.get(Constants.Plugins.CLUSTER_INFO_COLLECTOR_CLASS))
      .asSubclass(ClusterInfoCollector.class).newInstance();
    Map<String, String> clusterInfoCollectorConf = new HashMap<>();
    for (Map.Entry<String, String> entry :
      conf.getValByRegex(Constants.Plugins.CLUSTER_INFO_COLLECTOR_CONF_PREFIX + "*").entrySet()) {
      clusterInfoCollectorConf.put(entry.getKey().replace(Constants.Plugins.CLUSTER_INFO_COLLECTOR_CONF_PREFIX, ""),
                                   entry.getValue());
    }
    clusterInfoCollector.initialize(clusterInfoCollectorConf);
    return clusterInfoCollector;
  }
}
