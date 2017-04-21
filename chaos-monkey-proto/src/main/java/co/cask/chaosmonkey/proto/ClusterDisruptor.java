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

package co.cask.chaosmonkey.proto;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Interface with methods for disrupting services on a cluster
 */
public interface ClusterDisruptor {

  /**
   * Runs a custom disruption on the given service
   *
   * @param service The name of the service to run the disruption against
   * @param disruptionName The name of the disruption to be run
   */
  void disrupt(String service, String disruptionName) throws Exception;

  /**
   * Runs a custom disruption on the given service based on given configurations
   *
   * @param service The name of the service to run the disruption against
   * @param disruptionName The name of the disurption to be run
   * @param actionArguments Configuration for the action
   */
  void disrupt(String service, String disruptionName, ActionArguments actionArguments) throws Exception;

  /**
   * Starts the specified service
   *
   * @param service The name of the service to be started
   */
  void start(String service) throws Exception;

  /**
   * Starts the specified service based on given configurations
   *
   * @param service The name of the service to be started
   * @param actionArguments Configuration for the action
   */
  void start(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Restarts the specified service
   *
   * @param service The name of the service to be restarted
   */
  void restart(String service) throws Exception;

  /**
   * Restarts the specified service based on given configurations
   *
   * @param service The name of the service to be restarted
   * @param actionArguments Configuration for the action
   */
  void restart(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Stops the specified service
   *
   * @param service The name of the service to be stopped
   */
  void stop(String service) throws Exception;

  /**
   * Stops the specified service on based on given configurations
   *
   * @param service The name of the service to be stopped
   * @param actionArguments Configuration for the action
   */
  void stop(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Terminates the specified service
   *
   * @param service The name of the service to be terminated
   */
  void terminate(String service) throws Exception;

  /**
   * Terminates the specified service based on given configurations
   *
   * @param service The name of the service to be terminated
   * @param actionArguments Configuration for the action
   */
  void terminate(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Kills the specified service
   *
   * @param service The name of the service to be killed
   */
  void kill(String service) throws Exception;

  /**
   * Kills the specified service based on given configurations
   *
   * @param service The name of the service to be killed
   * @param actionArguments Configuration for the action
   */
  void kill(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Starts a rolling restart of the specified service using default
   *
   * @param service The name of the service to perform rolling restart on
   */
  void rollingRestart(String service) throws Exception;

  /**
   * Starts a rolling restart of the specified service using given configurations
   *
   * @param service The name of the service to perform rolling restart on
   * @param actionArguments Configuration for the action
   * @throws Exception
   */
  void rollingRestart(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Returns whether the specified service is undergoing start
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isStartRunning(String service) throws Exception;

  /**
   * Starts a service and waits for action to complete
   *
   * @param service The name of the service to be queried
   * @param timeout Time until timeout
   * @param timeoutUnit Unit of time for timeout
   * @throws Exception
   */
  void startAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception;

  /**
   * Returns whether the specified service is undergoing restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isRestartRunning(String service) throws Exception;

  /**
   * Restarts a service and waits for action to complete
   *
   * @param service The name of the service to be queried
   * @param timeout Time until timeout
   * @param timeoutUnit Unit of time for timeout
   * @throws Exception
   */
  void restartAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception;

  /**
   * Returns whether the specified service is undergoing stop
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isStopRunning(String service) throws Exception;

  /**
   * Stops a service and waits for action to complete
   *
   * @param service The name of the service to be queried
   * @param timeout Time until timeout
   * @param timeoutUnit Unit of time for timeout
   * @throws Exception
   */
  void stopAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception;

  /**
   * Returns whether the specified service is undergoing terminate
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isTerminateRunning(String service) throws Exception;

  /**
   * Terminates a service and waits for action to complete
   *
   * @param service The name of the service to be queried
   * @param timeout Time until timeout
   * @param timeoutUnit Unit of time for timeout
   * @throws Exception
   */
  void terminateAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception;

  /**
   * Returns whether the specified service is undergoing kill
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isKillRunning(String service) throws Exception;

  /**
   * Kills a service and waits for action to complete
   *
   * @param service The name of the service to be queried
   * @param timeout Time until timeout
   * @param timeoutUnit Unit of time for timeout
   * @throws Exception
   */
  void killAndWait(String service, long timeout, TimeUnit timeoutUnit) throws Exception;

  /**
   * Returns whether the specified service is undergoing rolling restart
   *
   * @param service The name of the service to be queried
   * @return true if running, false otherwise
   */
  boolean isRollingRestartRunning(String service) throws Exception;

  /**
   * Starts rolling restart on a service and wait for completion
   *
   * @param service The name of the service to be queried
   * @param actionArguments Optional arguments
   * @throws InterruptedException
   */
  void rollingRestartAndWait(String service, ActionArguments actionArguments) throws Exception;

  /**
   * Returns whether an action is running on the given service
   *
   * @param service The name of the service to be queried
   * @param action The name of disruption to be queried
   * @return true if running, false otherwise
   */
  boolean isActionRunning(String service, String action) throws Exception;

  /**
   * Gets the status of all configured services on each node of a cluster
   *
   * @return collection of {@link NodeStatus}
   */
  Collection<NodeStatus> getAllStatuses() throws Exception;

  /**
   * Gets the status of all services on a specified node
   *
   * @param ipAddress ip address of node to query status of
   * @return status of services in specified node, given in the form of {@link NodeStatus}
   */
  NodeStatus getStatus(String ipAddress) throws Exception;

  /**
   * Gets a {@link Map} corresponding available disruptions to each service
   *
   * @return
   * @throws Exception
   */
  Map<String, Collection<String>> getDisruptions() throws Exception;
}
