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

package co.cask.chaosmonkey.client;

import co.cask.chaosmonkey.client.config.ClientConfig;
import co.cask.chaosmonkey.client.config.ConnectionConfig;
import co.cask.chaosmonkey.proto.NodeStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;

/**
 *
 */
public class ChaosMonkeyClient {
  private static final Type STATUSES_TYPE = new TypeToken<List<NodeStatus>>() { }.getType();
  private static final Gson GSON = new Gson();

  private final HttpClient client;
  private final ClientConfig config;

  public ChaosMonkeyClient(ClientConfig config) {
    this(config, new DefaultHttpClient());
  }

  public ChaosMonkeyClient(ClientConfig config, HttpClient client) {
    this.config = config;
    this.client = client;
  }

  public List<NodeStatus> getAllStatuses() throws IOException {
    URI uri = config.getConnectionConfig().resolveURI("v1", "status");
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = client.execute(httpGet);

    List<NodeStatus> statuses;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
      statuses = GSON.fromJson(reader, STATUSES_TYPE);
    }
    return statuses;
  }

  public static void main(String[] args) throws Exception {
    ConnectionConfig connectionConfig = new ConnectionConfig("localhost", 11020, false);
    ClientConfig clientConfig = new ClientConfig.Builder()
      .setDefaultReadTimeout(60 * 1000)
      .setUploadReadTimeout(120 * 1000)
      .setConnectionConfig(connectionConfig).build();

    ChaosMonkeyClient client = new ChaosMonkeyClient(clientConfig);
    for (NodeStatus status : client.getAllStatuses()) {
      System.out.println(status.ipAddress);
    }
  }
}
