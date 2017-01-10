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
 * TODO: procrastinate on documentation
 */
public enum Services {
  HBaseRegionServer("/hbase/hbase-hbase-regionserver.pid"),
  HBaseMaster("/hbase/hbase-hbase-master.pid"),
  ZookeeperServer("/zookeeper/zookeeper-server.pid");

  final String path;
  final String baseDirectory = "/var/run";

  Services(String path) { this.path = path; }

  public String getPath() { return baseDirectory + path; }
}
