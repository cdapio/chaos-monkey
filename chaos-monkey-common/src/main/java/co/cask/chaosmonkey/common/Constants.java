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

package co.cask.chaosmonkey.common;

import com.google.common.collect.ImmutableSet;

/**
 * List of constants used by ChaosMonkey.
 */
public class Constants {

  /**
   * Constants related to {@code Server}.
   */
  public static final class Server {
    public static final String API_VERSION_1_TOKEN = "v1";
    public static final String API_VERSION_1 = "/" + API_VERSION_1_TOKEN;
    public static final int PORT = 11020;
  }

  /**
   * Constants related to {@code RemoteProcess}.
   */
  public static final class RemoteProcess {
    public static final int SIGKILL = 9;
    public static final int SIGTERM = 15;
    public static final String START = "start";
    public static final String RESTART = "restart";
    public static final String STOP = "stop";
    public static final String TERMINATE = "terminate";
    public static final String KILL = "kill";
    public static final String IS_RUNNING = "isRunning";
    public static final String EXISTS = "exists";
    public static final ImmutableSet<String> CONFIG_OPTIONS = ImmutableSet.of(
      Constants.RemoteProcess.START,
      Constants.RemoteProcess.RESTART,
      Constants.RemoteProcess.STOP,
      Constants.RemoteProcess.TERMINATE,
      Constants.RemoteProcess.KILL,
      Constants.RemoteProcess.IS_RUNNING,
      Constants.RemoteProcess.EXISTS
    );
  }

  /**
   * Constants related to Coopr, used for retrieving cluster information.
   */
  public static final class Coopr {
    public static final String USER_ID = "coopr.userId";
    public static final String TENANT_ID = "coopr.tenantId";
    public static final String SERVER_URI = "coopr.server.uri";
    public static final String API_VERSION = "coopr.api.version";
    public static final String CLUSTER_ID = "coopr.clusterId";
  }

  /**
   * Constants related to plugins for Chaos Monkey
   */
  public static final class Plugins {
    public static final String CLUSTER_INFO_COLLECTOR_CLASS = "cluster.info.collector.class";
    public static final String CLUSTER_INFO_COLLECTOR_CONF_PREFIX = "cluster.info.collector.";
  }
}
