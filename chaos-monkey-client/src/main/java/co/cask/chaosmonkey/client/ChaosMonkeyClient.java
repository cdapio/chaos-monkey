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
import co.cask.chaosmonkey.common.BadRequestException;
import co.cask.chaosmonkey.common.Constants;
import co.cask.chaosmonkey.common.InternalServerErrorException;
import co.cask.chaosmonkey.common.NotFoundException;
import co.cask.chaosmonkey.proto.NodeProperties;
import co.cask.chaosmonkey.proto.NodeStatus;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;

/**
 * Provides ways to interact with Chaos Monkey.
 */
public class ChaosMonkeyClient {
  private static final Type STATUSES_TYPE = new TypeToken<List<NodeStatus>>() { }.getType();
  private static final Type PROPERTIES_TYPE = new TypeToken<List<NodeProperties>>() { }.getType();
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

  /**
   * Starts the specified service
   *
   * @param service The name of the service to be started
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void start(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "start");
  }

  /**
   * Restarts the specified service
   *
   * @param service The name of the service to be restarted
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void restart(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "restart");
  }

  /**
   * Stops the specified service
   *
   * @param service The name of the service to be stopped
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void stop(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "stop");
  }

  /**
   * Terminates the specified service
   *
   * @param service The name of the service to be terminated
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void terminate(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "terminate");
  }

  /**
   * Kills the specified service
   *
   * @param service The name of the service to be killed
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void kill(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "kill");
  }

  private void executeAction(String service, String action)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    URI uri = config.getConnectionConfig().resolveURI(Constants.Server.API_VERSION_1_TOKEN,
                                                      "services/" + service + "/" + action);
    HttpPost httpPost = new HttpPost(uri);
    HttpResponse response = client.execute(httpPost);

    int responseCode = response.getStatusLine().getStatusCode();
    String reasonPhrase = response.getStatusLine().getReasonPhrase();
    EntityUtils.consume(response.getEntity());
    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new NotFoundException("Service not found: " + service);
    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(String.format("Bad Request. Reason: %s", reasonPhrase));
    } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
      throw new InternalServerErrorException(String.format("Internal Error. Reason: %s", reasonPhrase));
    }
  }

  //TODO: add request body and rolling restart status
  public void rollingRestart(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "rolling-restart");
  }

  /**
   * Gets the status of all configured services on each node of a cluster
   *
   * @return list of {@link NodeStatus}
   * @throws IOException if a network error occurred
   */
  public List<NodeStatus> getAllStatuses() throws IOException {
    URI uri = config.getConnectionConfig().resolveURI(Constants.Server.API_VERSION_1_TOKEN, "status");
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = client.execute(httpGet);

    List<NodeStatus> statuses;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
      statuses = GSON.fromJson(reader, STATUSES_TYPE);
    }
    return statuses;
  }

  /**
   * Gets the status of all services on a specified node
   *
   * @param ipAddress ip address of node to query status of
   * @return status of services in specified node, given in the form of {@link NodeStatus}
   * @throws IOException if a network error occurred
   */
  public NodeStatus getStatus(String ipAddress) throws IOException {
    URI uri = config.getConnectionConfig().resolveURI(Constants.Server.API_VERSION_1_TOKEN,
                                                      "nodes/" + ipAddress + "/status");
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = client.execute(httpGet);

    NodeStatus status;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
      status = GSON.fromJson(reader, NodeStatus.class);
    }
    return status;
  }

  /**
   * Gets information about each node in the configured cluster
   *
   * @return list of {@link NodeProperties}
   * @throws IOException if a network error occurred
   */
  public List<NodeProperties> getNodeProperties() throws IOException {
    URI uri = config.getConnectionConfig().resolveURI(Constants.Server.API_VERSION_1_TOKEN, "nodes");
    HttpGet httpGet = new HttpGet(uri);
    HttpResponse response = client.execute(httpGet);

    List<NodeProperties> nodePropertiesList;
    try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {
      nodePropertiesList = GSON.fromJson(reader, PROPERTIES_TYPE);
    }
    return  nodePropertiesList;
  }
}
