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

import co.cask.chaosmonkey.client.ChaosMonkeyClient;
import co.cask.chaosmonkey.proto.NodeStatus;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * An implementation of ChaosMonkeyClient that uses {@link ChaosMonkeyService} to perform disruptions
 */
public class ChaosMonkeyDefaultClient implements ChaosMonkeyClient {

  private final ChaosMonkeyService chaosMonkeyService;

  public ChaosMonkeyDefaultClient(ChaosMonkeyService chaosMonkeyService) {
    this.chaosMonkeyService = chaosMonkeyService;
  }

  /**
   * Starts the specified service
   *
   * @param service The name of the service to be started
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void start(String service) {
    chaosMonkeyService.executeAction(service, "start", null, null, null, null, null);
  }

  /**
   * Starts the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be started
   * @param count The number of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void start(String service, int count) {
    chaosMonkeyService.executeAction(service, "start", null, count, null, null, null);
  }

  /**
   * Starts the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be started
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void start(String service, double percentage) {
    chaosMonkeyService.executeAction(service, "start", null, null, percentage, null, null);
  }

  /**
   * Starts the specified service on given nodes
   *
   * @param service The name of the service to be started
   * @param nodes Collection of ip addresses of affected nodes
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void start(String service, Collection<String> nodes) {
    chaosMonkeyService.executeAction(service, "start", nodes, null, null, null, null);
  }

  /**
   * Restarts the specified service
   *
   * @param service The name of the service to be restarted
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void restart(String service) {
    chaosMonkeyService.executeAction(service, "restart", null, null, null, null, null);
  }

  /**
   * Restarts the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be restarted
   * @param count The number of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void restart(String service, int count) {
    chaosMonkeyService.executeAction(service, "restart", null, count, null, null, null);
  }

  /**
   * Restarts the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be restarted
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void restart(String service, double percentage) {
    chaosMonkeyService.executeAction(service, "restart", null, null, percentage, null, null);
  }

  /**
   * Restarts the specified service on given nodes
   *
   * @param service The name of the service to be restarted
   * @param nodes Collection of ip addresses of affected nodes
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void restart(String service, Collection<String> nodes) {
    chaosMonkeyService.executeAction(service, "restart", nodes, null, null, null, null);
  }

  /**
   * Stops the specified service
   *
   * @param service The name of the service to be stopped
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void stop(String service) {
    chaosMonkeyService.executeAction(service, "stop", null, null, null, null, null);
  }

  /**
   * Stops the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be stopped
   * @param count The number of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void stop(String service, int count) {
    chaosMonkeyService.executeAction(service, "stop", null, count, null, null, null);
  }

  /**
   * Stops the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be stopped
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void stop(String service, double percentage) {
    chaosMonkeyService.executeAction(service, "stop", null, null, percentage, null, null);
  }

  /**
   * Stops the specified service on given nodes
   *
   * @param service The name of the service to be stopped
   * @param nodes Collection of ip addresses of affected nodes
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void stop(String service, Collection<String> nodes) {
    chaosMonkeyService.executeAction(service, "stop", nodes, null, null, null, null);
  }

  /**
   * Terminates the specified service
   *
   * @param service The name of the service to be terminated
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void terminate(String service) {
    chaosMonkeyService.executeAction(service, "terminate", null, null, null, null, null);
  }

  /**
   * Terminates the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be terminated
   * @param count The number of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void terminate(String service, int count) {
    chaosMonkeyService.executeAction(service, "terminate", null, count, null, null, null);
  }

  /**
   * Terminates the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be terminated
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void terminate(String service, double percentage) {
    chaosMonkeyService.executeAction(service, "terminate", null, null, percentage, null, null);
  }

  /**
   * Terminates the specified service on given nodes
   *
   * @param service The name of the service to be terminated
   * @param nodes Collection of ip addresses of affected nodes
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void terminate(String service, Collection<String> nodes) {
    chaosMonkeyService.executeAction(service, "terminate", nodes, null, null, null, null);
  }

  /**
   * Kills the specified service
   *
   * @param service The name of the service to be killed
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void kill(String service) {
    chaosMonkeyService.executeAction(service, "kill", null, null, null, null, null);
  }

  /**
   * Kills the specified service on 'count' nodes chosen arbitrarily
   *
   * @param service The name of the service to be killed
   * @param count The number of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void kill(String service, int count) {
    chaosMonkeyService.executeAction(service, "kill", null, count, null, null, null);
  }

  /**
   * Kills the specified service on percentage of nodes chosen arbitrarily
   *
   * @param service The name of the service to be killed
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void kill(String service, double percentage) {
    chaosMonkeyService.executeAction(service, "kill", null, null, percentage, null, null);
  }

  /**
   * Kills the specified service on given nodes
   *
   * @param service The name of the service to be killed
   * @param nodes Collection of ip addresses of affected nodes
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void kill(String service, Collection<String> nodes) {
    chaosMonkeyService.executeAction(service, "kill", nodes, null, null, null, null);
  }

  /**
   * Starts a rolling restart of the specified service
   *
   * @param service The name of the service to perform rolling restart on
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void rollingRestart(String service) {
    chaosMonkeyService.executeAction(service, "rolling-restart", null, null, null, null, null);
  }

  /**
   * Starts a rolling restart of the specified service using given restart time and delay
   *
   * @param service The name of the service to perform rolling restart on
   * @param restartTimeSeconds Number of seconds a service is kept offline before restarting
   * @param delaySeconds Number of seconds between restarting each service
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void rollingRestart(String service, int restartTimeSeconds, int delaySeconds) {
    chaosMonkeyService.executeAction(service, "rolling-restart", null, null, null, restartTimeSeconds, delaySeconds);
  }

  /**
   * Starts a rolling restart of the specified service using given restart time and delay
   *
   * @param service The name of the service to perform rolling restart on
   * @param count The number of nodes affected
   * @param restartTimeSeconds Number of seconds a service is kept offline before restarting
   * @param delaySeconds Number of seconds between restarting each service
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void rollingRestart(String service, int count, int restartTimeSeconds, int delaySeconds) {
    chaosMonkeyService.executeAction(service, "rolling-restart", null, count, null, restartTimeSeconds, delaySeconds);
  }

  /**
   * Starts a rolling restart of the specified service using given restart time and delay
   *
   * @param service The name of the service to perform rolling restart on
   * @param percentage Number between 0 and 1 to represent the percent of nodes affected
   * @param restartTimeSeconds Number of seconds a service is kept offline before restarting
   * @param delaySeconds Number of seconds between restarting each service
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void rollingRestart(String service, double percentage, int restartTimeSeconds, int delaySeconds) {
    chaosMonkeyService.executeAction(service, "rolling-restart", null, null, percentage, restartTimeSeconds,
                                     delaySeconds);
  }

  /**
   * Starts a rolling restart of the specified service using given restart time and delay
   *
   * @param service The name of the service to perform rolling restart on
   * @param nodes Collection of ip addresses of affected nodes
   * @param restartTimeSeconds Number of seconds a service is kept offline before restarting
   * @param delaySeconds Number of seconds between restarting each service
   * @throws BadRequestException if nodes, count, or percentage contain invalid values
   * @throws NotFoundException if service or action are not found
   * @throws IllegalStateException if the same disruption is already running
   */
  @Override
  public void rollingRestart(String service, Collection<String> nodes, int restartTimeSeconds, int delaySeconds) {
    chaosMonkeyService.executeAction(service, "rolling-restart", nodes, null, null, restartTimeSeconds, delaySeconds);
  }

  /**
   * Returns whether the specified service is undergoing rolling restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  @Override
  public boolean isRollingRestartRunning(String service) {
    return isActionRunning(service, "rolling-restart");
  }

  /**
   * Returns whether an action is running on the given service
   *
   * @param service The name of the service to be queried
   * @param action The name of the action to be queried
   * @return true if running, false otherwise
   */
  @Override
  public boolean isActionRunning(String service, String action) {
    return chaosMonkeyService.getActionStatus(service, action).isRunning();
  }

  /**
   * Blocks execution until rolling restart is done on specified service.
   * Rolling restart status is checked every 1 second
   *
   * @param service The name of the service to be queried
   * @throws InterruptedException
   */
  @Override
  public void waitForRollingRestart(String service) throws InterruptedException {
    while (isRollingRestartRunning(service)) {
      TimeUnit.SECONDS.sleep(1);
    }
  }

  /**
   * Gets the status of all configured services on each node of a cluster
   *
   * @return collection of {@link NodeStatus}
   */
  @Override
  public Collection<NodeStatus> getAllStatuses() throws Exception {
    return chaosMonkeyService.getNodeStatuses();
  }

  /**
   * Gets the status of all services on a specified node
   *
   * @param ipAddress ip address of node to query status of
   * @return status of services in specified node, given in the form of {@link NodeStatus}
   */
  @Override
  public NodeStatus getStatus(String ipAddress) throws Exception {
    return chaosMonkeyService.getNodeStatus(ipAddress);
  }
}
