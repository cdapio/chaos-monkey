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
import co.cask.chaosmonkey.proto.ActionArguments;
import co.cask.chaosmonkey.proto.ActionStatus;
import co.cask.chaosmonkey.proto.ClusterDisruptor;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

/**
 * Provides ways to interact with Chaos Monkey.
 */
public class ClusterDisruptorClient implements ClusterDisruptor {
  private static final Type STATUSES_TYPE = new TypeToken<Collection<NodeStatus>>() { }.getType();
  private static final Gson GSON = new Gson();

  private final String hostname;
  private final int port;
  private final boolean sslEnabled;

  public ClusterDisruptorClient(String hostname, int port) {
    this(hostname, port, false);
  }

  public ClusterDisruptorClient(String hostname, int port, boolean sslEnabled) {
    this.hostname = hostname;
    this.port = port;
    this.sslEnabled = sslEnabled;
  }

  @Override
  public void disrupt(String service, String disruptionName) throws Exception {
    disrupt(service, disruptionName, null);
  }

  @Override
  public void disrupt(String service, String disruptionName, @Nullable ActionArguments actionArguments)
    throws Exception {
    executeActionWithArgument(service, disruptionName, actionArguments);
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
  @Override
  public void start(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    start(service, null);
  }

  /**
   * Starts the specified service based on given configurations
   *
   * @param service The name of the service to be started
   * @param actionArguments Optional, the configuration for the action
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void start(String service, @Nullable ActionArguments actionArguments)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, Constants.RemoteProcess.START, actionArguments);
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
  @Override
  public void restart(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    restart(service, null);
  }

  /**
   * Restarts the specified service based on given configurations
   *
   * @param service The name of the service to be restarted
   * @param actionArguments Optional, the configuration for the action
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void restart(String service, @Nullable ActionArguments actionArguments)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, Constants.RemoteProcess.RESTART, actionArguments);
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
  @Override
  public void stop(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    stop(service, null);
  }

  /**
   * Stops the specified service based on given configurations
   *
   * @param service The name of the service to be stopped
   * @param actionArguments Optional, the configuration for the action
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void stop(String service, @Nullable ActionArguments actionArguments)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, Constants.RemoteProcess.STOP, actionArguments);
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
  @Override
  public void terminate(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    terminate(service, null);
  }

  /**
   * Terminates the specified service based on given configurations
   *
   * @param service The name of the service to be terminated
   * @param actionArguments Optional, the configuration for the action
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void terminate(String service, @Nullable ActionArguments actionArguments)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, Constants.RemoteProcess.TERMINATE, actionArguments);
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
  @Override
  public void kill(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    kill(service, null);
  }

  /**
   * Kills the specified service based on given configurations
   *
   * @param service The name of the service to be killed
   * @param actionArguments Optional, the configuration for the action
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void kill(String service, @Nullable ActionArguments actionArguments)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    executeActionWithArgument(service, Constants.RemoteProcess.KILL, actionArguments);
  }

  private void executeActionWithArgument(String service, String action, @Nullable ActionArguments actionArguments)
    throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action);
    HttpRequest request;
    if (actionArguments == null) {
      request = HttpRequest.post(url).build();
    } else {
      request = HttpRequest.post(url).withBody(GSON.toJson(actionArguments)).build();
    }
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

  /**
   * Starts a rolling restart of the specified service
   *
   * @param service The name of the service to perform rolling restart on
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void rollingRestart(String service)
    throws IOException, NotFoundException, BadRequestException, InternalServerErrorException {
    rollingRestartWithRequestBody(service, null);
  }

  /**
   * Starts a rolling restart with given configurations
   *
   * @param service The name of the service to perform rolling restart on
   * @param actionArguments Optional configuration for the rolling restart
   * @throws IOException if a network error occurred
   * @throws NotFoundException if specified service does not exist
   * @throws BadRequestException if invalid request body is provided
   * @throws InternalServerErrorException if internal server error occurred
   */
  @Override
  public void rollingRestart(String service, @Nullable ActionArguments actionArguments)
    throws IOException {
    if (actionArguments == null) {
      rollingRestart(service);
    }
    rollingRestartWithRequestBody(service, GSON.toJson(actionArguments));
  }

  private void rollingRestartWithRequestBody(String service, @Nullable String requestBody) throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/rolling-restart");
    HttpRequest request;
    if (requestBody == null) {
      request = HttpRequest.post(url).build();
    } else {
      request = HttpRequest.post(url).withBody(requestBody).build();
    }
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
   * Returns whether the specified service is undergoing start
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isStartRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.START);
  }

  @Override
  public void startAndWait(String service, long timeout, TimeUnit timeoutUnit)
    throws IOException, InterruptedException, TimeoutException {
    start(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isStartRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  /**
   * Returns whether the specified service is undergoing restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isRestartRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.RESTART);
  }

  @Override
  public void restartAndWait(String service, long timeout, TimeUnit timeoutUnit)
    throws IOException, InterruptedException, TimeoutException {
    restart(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isRestartRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  /**
   * Returns whether the specified service is undergoing stop
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isStopRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.STOP);
  }

  @Override
  public void stopAndWait(String service, long timeout, TimeUnit timeoutUnit)
    throws IOException, InterruptedException, TimeoutException {
    stop(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isStopRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  /**
   * Returns whether the specified service is undergoing terminate
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isTerminateRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.TERMINATE);
  }

  @Override
  public void terminateAndWait(String service, long timeout, TimeUnit timeoutUnit)
    throws IOException, InterruptedException, TimeoutException {
    terminate(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isTerminateRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  /**
   * Returns whether the specified service is undergoing kill
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isKillRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.KILL);
  }

  @Override
  public void killAndWait(String service, long timeout, TimeUnit timeoutUnit)
    throws IOException, InterruptedException, TimeoutException {
    kill(service);
    long startTime = System.currentTimeMillis();
    long timeoutMs = timeoutUnit.toMillis(timeout);
    while (System.currentTimeMillis() - startTime < timeoutMs) {
      if (!isKillRunning(service)) {
        return;
      }
      TimeUnit.SECONDS.sleep(1);
    }
    throw new TimeoutException(String.format("Timeout occurred after %d %s", timeout, timeoutUnit.name()));
  }

  /**
   * Returns whether the specified service is undergoing rolling restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isRollingRestartRunning(String service) throws IOException {
    return isActionRunning(service, Constants.RemoteProcess.ROLLING_RESTART);
  }

  /**
   * Blocks execution until rolling restart is done on specified service.
   * Rolling restart status is checked every 1 second
   *
   * @param service The name of the service to be queried
   * @param actionArguments Optional configuration for the rolling restart
   * @throws IOException if a network error occurred
   * @throws InterruptedException
   */
  @Override
  public void rollingRestartAndWait(String service, @Nullable ActionArguments actionArguments)
    throws IOException, InterruptedException {
    rollingRestart(service, actionArguments);
    while (isRollingRestartRunning(service)) {
      TimeUnit.SECONDS.sleep(1);
    }
  }

  /**
   * Returns whether an action is running on the given service
   *
   * @param service The name of the service to be queried
   * @param action The name of the action to be queried
   * @return true if running, false otherwise
   * @throws IOException if a network error occurred
   */
  @Override
  public boolean isActionRunning(String service, String action) throws IOException {
    URL url = resolveURL(Constants.Server.API_VERSION_1_TOKEN, "services/" + service + "/" + action + "/status");
    HttpRequest request = HttpRequest.get(url).build();
    HttpResponse response = HttpRequests.execute(request);

    return GSON.fromJson(response.getResponseBodyAsString(), ActionStatus.class).isRunning();
  }

  /**
   * Gets the status of all configured services on each node of a cluster
   *
   * @return list of {@link NodeStatus}
   * @throws IOException if a network error occurred
   */
  @Override
  public Collection<NodeStatus> getAllStatuses() throws IOException {
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
  @Override
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
