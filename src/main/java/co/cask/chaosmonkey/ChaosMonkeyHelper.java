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


import co.cask.chaosmonkey.conf.Configuration;
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
 * Helper methods for chaos monkey
 */
public class ChaosMonkeyHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ChaosMonkeyHelper.class);
  private static final Gson GSON = new Gson();
  private static final Type NODES_TYPE = new TypeToken<Map<String, NodeProperties>>() { }.getType();

  /**
   * Gets the Map of NodeProperties for each node in a given cluster
   * @param clusterId The cluster ID to query for
   * @return Map of NodeProperties
   * @throws IOException
   */
  public static Map<String, NodeProperties> getNodeProperties(String clusterId, Configuration conf) throws IOException {
    HttpClient client = new DefaultHttpClient();

    HttpPost httpPost = new HttpPost(conf.get(Constants.Coopr.SERVERURI) + "/" +
                                       conf.get(Constants.Coopr.APIVERSION) + "/" +
                                       "getNodeProperties");
    httpPost.setHeader("coopr-userid", conf.get(Constants.Coopr.USERID));
    httpPost.setHeader("coopr-tenantid", conf.get(Constants.Coopr.TENANTID));
    httpPost.setEntity(new ByteArrayEntity(String.format("{\"clusterId\":\"%s\"}", clusterId).getBytes("UTF-8")));

    HttpResponse response = client.execute(httpPost);

    Map<String, NodeProperties> nodes;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
       nodes = GSON.fromJson(reader, NODES_TYPE);
    }

    return nodes;
  }
}
