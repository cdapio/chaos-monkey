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
import co.cask.chaosmonkey.proto.ActionStatus;
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
   * Starts the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be started
   * @param count The number of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void start(String service, int count)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "start", "count", Integer.toString(count));
  }

  /**
   * Starts the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be started
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void start(String service, double percentage)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "start", "percentage", Double.toString(percentage));
  }

  /**
   * Starts the specified service on given nodes
   *
   * @param service The name of the service to be started
   * @param nodes Collection of ip addresses of affected nodes
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void start(String service, Collection<String> nodes)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    String collectionJSON = GSON.toJson(nodes);
    executeActionWithArgument(service, "start", "nodes", collectionJSON);
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
   * Restarts the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be restarted
   * @param count The number of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void restart(String service, int count)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "restart", "count", Integer.toString(count));
  }

  /**
   * Restarts the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be restarted
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void restart(String service, double percentage)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "restart", "percentage", Double.toString(percentage));
  }

  /**
   * Restarts the specified service on given nodes
   *
   * @param service The name of the service to be restarted
   * @param nodes Collection of ip addresses of affected nodes
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void restart(String service, Collection<String> nodes)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    String collectionJSON = GSON.toJson(nodes);
    executeActionWithArgument(service, "restart", "nodes", collectionJSON);
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
   * Stops the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be stopped
   * @param count The number of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void stop(String service, int count)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "stop", "count", Integer.toString(count));
  }

  /**
   * Stops the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be stopped
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void stop(String service, double percentage)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "stop", "percentage", Double.toString(percentage));
  }

  /**
   * Stops the specified service on given nodes
   *
   * @param service The name of the service to be stopped
   * @param nodes Collection of ip addresses of affected nodes
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void stop(String service, Collection<String> nodes)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    String collectionJSON = GSON.toJson(nodes);
    executeActionWithArgument(service, "stop", "nodes", collectionJSON);
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
   * Terminates the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be terminated
   * @param count The number of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void terminate(String service, int count)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "terminate", "count", Integer.toString(count));
  }

  /**
   * Terminates the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be terminated
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void terminate(String service, double percentage)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "terminate", "percentage", Double.toString(percentage));
  }

  /**
   * Terminates the specified service on given nodes
   *
   * @param service The name of the service to be terminated
   * @param nodes Collection of ip addresses of affected nodes
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void terminate(String service, Collection<String> nodes)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    String collectionJSON = GSON.toJson(nodes);
    executeActionWithArgument(service, "terminate", "nodes", collectionJSON);
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

  /**
   * Kills the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be killed
   * @param count The number of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void kill(String service, int count)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "kill", "count", Integer.toString(count));
  }

  /**
   * Kills the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be killed
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void kill(String service, double percentage)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, "kill", "percentage", Double.toString(percentage));
  }

  /**
   * Kills the specified service on given nodes
   *
   * @param service The name of the service to be killed
   * @param nodes Collection of ip addresses of affected nodes
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void kill(String service, Collection<String> nodes)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    String collectionJSON = GSON.toJson(nodes);
    executeActionWithArgument(service, "kill", "nodes", collectionJSON);
  }

  private void executeAction(String service, String action)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action);
    HttpRequest request = HttpRequest.post(url).build();
    HttpResponse response = HttpRequests.execute(request);

    int responseCode = response.getResponseCode();
    String responseMessage = response.getResponseMessage();
    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new NotFoundException(String.format("Service not found: %s", service));
    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(String.format("Bad Request. Reason: %s", responseMessage));
    } else if (responseCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
      throw new InternalServerErrorException(String.format("Internal Error. Reason: %s", responseMessage));
    }
  }

  private void executeActionWithArgument(String service, String action, String field, String value)
    throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action);
    HttpRequest request = HttpRequest.post(url)
      .withBody(String.format("{%s:%s}", field, value)).build();
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

  /**
   * Starts a rolling restart of the specified service
   *
   * @param service The name of the service to perform rolling restart on
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  public void rollingRestart(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeAction(service, "rolling-restart");
  }

  /**
   * Starts a rolling restart of the specified service using given restart time and delay
   *
   * @param service The name of the service to perform rolling restart on
   * @param restartTimeSeconds Number of seconds a service is kept offline before restarting
   * @param delaySeconds Number of seconds between restarting each service
   * @throws IOException if a network error occurred
   */
  public void rollingRestart(String service, int restartTimeSeconds, int delaySeconds)
    throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/rolling-restart");
    HttpRequest request = HttpRequest.post(url)
      .withBody(String.format("{restartTime:%d,delay:%d}", restartTimeSeconds, delaySeconds)).build();
    HttpResponse response = HttpRequests.execute(request);

    int responseCode = response.getResponseCode();
    String responseMessage = response.getResponseMessage();
    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
      throw new NotFoundException(String.format("Service not found: %s", service));
    } else if (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) {
      throw new BadRequestException(String.format("Bad Request. Reason: %s", responseMessage));
    }
  }

  /**
   * Returns whether the specified service is undergoing rolling restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  public boolean isRollingRestartRunning(String service) throws IOException {
    return isActionRunning(service, "rolling-restart");
  }

  /**
   * Returns whether an action is running on the given service
   *
   * @param service The name of the service to be queried
   * @param action The name of the action to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  public boolean isActionRunning(String service, String action) throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action + "/status");
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = HttpRequests.execute(request);

    return GSON.fromJson(response.getResponseBodyAsString(), ActionStatus.class).isRunning();
  }

  /**
   * Blocks execution until rolling restart is done on specified service.
   * Rolling restart status is checked every 1 second
   *
   * @param service The name of the service to be queried
   * @throws IOException if a network error occurred
   * @throws InterruptedException
   */
  public void waitForRollingRestart(String service) throws IOException, InterruptedException {
    while (isRollingRestartRunning(service)) {
      TimeUnit.SECONDS.sleep(1);
    }
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
