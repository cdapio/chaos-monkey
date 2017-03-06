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
import co.cask.chaosmonkey.proto.NodeProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Helper methods for ChaosMonkey.
 */
public class ChaosMonkeyHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ChaosMonkeyHelper.class);
  private static final Gson GSON = new Gson();
  private static final Type NODES_TYPE = new TypeToken<Map<String, NodeProperties>>() { }.getType();

  /**
   * Gets the Map of NodeProperties for each node in a given cluster
   * @return Map of NodeProperties
   * @throws IOException If an invalid cluster ID is given
   */
  public static Map<String, NodeProperties> getNodeProperties(Configuration conf) throws IOException {
    HttpClient client = new DefaultHttpClient();

    String clusterId = conf.get(Constants.Coopr.CLUSTER_ID);
    if (clusterId == null || clusterId.isEmpty()) {
      throw new IllegalArgumentException("Cluster ID not specified");
    }

    HttpPost httpPost = new HttpPost(conf.get(Constants.Coopr.SERVER_URI) + "/" +
                                       conf.get(Constants.Coopr.API_VERSION) + "/" +
                                       "getNodeProperties");
    httpPost.setHeader("coopr-userid", conf.get(Constants.Coopr.USER_ID));
    httpPost.setHeader("coopr-tenantid", conf.get(Constants.Coopr.TENANT_ID));
    httpPost.setEntity(new ByteArrayEntity(String.format("{\"clusterId\":\"%s\"}", clusterId)
                                             .getBytes("UTF-8")));

    HttpResponse response = client.execute(httpPost);

    Map<String, NodeProperties> nodes;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
       nodes = GSON.fromJson(reader, NODES_TYPE);
    }

    return nodes;
  }
}
