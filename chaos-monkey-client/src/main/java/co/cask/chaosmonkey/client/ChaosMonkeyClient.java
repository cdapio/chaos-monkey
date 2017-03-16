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

import co.cask.chaosmonkey.common.Constants;
import co.cask.chaosmonkey.proto.NodeProperties;
import co.cask.chaosmonkey.proto.NodeStatus;
import co.cask.common.http.HttpRequest;
import co.cask.common.http.HttpRequests;
import co.cask.common.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

/**
 * Provides ways to interact with Chaos Monkey.
 */
public class ChaosMonkeyClient {
  private static final Type STATUSES_TYPE = new TypeToken<List<NodeStatus>>() { }.getType();
  private static final Type PROPERTIES_TYPE = new TypeToken<List<NodeProperties>>() { }.getType();
  private static final Gson GSON = new Gson();

  private final String hostname;
  private final int port;
  private final boolean sslEnabled;

  public ChaosMonkeyClient(String hostname, int port) {
    this(hostname, port, false);
  }

  public ChaosMonkeyClient(String hostname, int port, boolean sslEnabled) {
    this.hostname = hostname;
    this.port = port;
    this.sslEnabled = sslEnabled;
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
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action);
    HttpRequest request = HttpRequest.post(url).build();
    HttpResponse response = HttpRequests.execute(request);

    int responseCode = response.getResponseCode();
    String responseMessage = response.getResponseMessage();
    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new NotFoundException("Service not found: " + service);
    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(String.format("Bad Request. Reason: %s", responseMessage));
    } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
      throw new InternalServerErrorException(String.format("Internal Error. Reason: %s", responseMessage));
    }
  }

  //TODO: add request body and rolling restart status
  /**
   * Starts a rolling restart of the specified service
   *
   * @param service The name of the service to be rolling restarted
   * @throws IOException if a network error occurrred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
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
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "status");
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = HttpRequests.execute(request);

    return GSON.fromJson(response.getResponseBodyAsString(), STATUSES_TYPE);
  }

  /**
   * Gets the status of all services on a specified node
   *
   * @param ipAddress ip address of node to query status of
   * @return status of services in specified node, given in the form of {@link NodeStatus}
   * @throws IOException if a network error occurred
   */
  public NodeStatus getStatus(String ipAddress) throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "nodes/" + ipAddress + "/status");
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = HttpRequests.execute(request);

    return GSON.fromJson(response.getResponseBodyAsString(), NodeStatus.class);
  }

  private String getURL() throws MalformedURLException {
    return String.format("%s://%s:%d", sslEnabled ? "https" : "http", hostname, port);
  }

  private URL resolveURL(String apiVersion, String path) throws MalformedURLException {
    return new URL(getURL() + String.format("/%s/%s", apiVersion, path));
  }
}
