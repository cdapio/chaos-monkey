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
import co.cask.chaosmonkey.proto.NodeProperties;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpRequests;
import co.cask.common.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collect cluster information from Coopr
 */
public class CooprInfoCollector implements ClusterInfoCollector {
  private static final Gson GSON = new Gson();
  private static final Type NODES_TYPE = new TypeToken<Map<String, NodeProperties>>() { }.getType();
  private Map<String, String> properties = new HashMap<>();

  @Override
  public void initialize(Map<String, String> properties) throws Exception {
    this.properties = properties;
  }

  @Override
  public Collection<NodeProperties> getNodeProperties() throws Exception {
    String clusterId = properties.get(Constants.Coopr.CLUSTER_ID);
    if (clusterId == null || clusterId.isEmpty()) {
      throw new IllegalArgumentException("Cluster ID not specified");
    }

    URL url = new URL(properties.get(Constants.Coopr.SERVER_URI) + "/" +
                        properties.get(Constants.Coopr.API_VERSION) + "/getNodeProperties");
    HttpRequest request = HttpRequest.get(url)
      .addHeader("coopr-userid", properties.get(Constants.Coopr.USER_ID))
      .addHeader("coopr-tenantid", properties.get(Constants.Coopr.TENANT_ID))
      .withBody(String.format("{\"clusterId\":\"%s\"}", clusterId)).build();
    HttpResponse response = HttpRequests.execute(request);

    Map<String, NodeProperties> nodes =  GSON.fromJson(response.getResponseBodyAsString(), NODES_TYPE);
    return nodes.values();
  }
}
