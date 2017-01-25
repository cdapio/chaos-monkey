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

/**
 * List of constants used by chaos monkey
 */
public class Constants {

  /**
   * Constants related to {@code RemoteProcess}.
   */
  public static final class RemoteProcess {
    public static final int SIGKILL = 9;
    public static final int SIGTERM = 15;
  }

  /**
   * Constants related to Coopr, used for retrieving cluster information
   */
  public static final class Coopr {
    public static final String USER_ID = "coopr.userId";
    public static final String TENANT_ID = "coopr.tenantId";
    public static final String SERVER_URI = "coopr.server.uri";
    public static final String API_VERSION = "coopr.api.version";
    public static final String CLUSTER_ID = "coopr.clusterId";
  }
}
