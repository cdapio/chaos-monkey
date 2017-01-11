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

import com.google.common.collect.ImmutableMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: procrastinate on documentation
 */
public class Service {

  public enum ServiceName {
    HBaseRegionServer("HBaseRegionServer"),
    HBaseMaster("HBaseMaster"),
    ZookeeperServer("ZookeeperServer"),
    MySQLServer("MySQLServer"),
    HiveMetastore("HiveMetastore"),
    HadoopYarnResourceManager("HadoopYarnResourceManager"),
    HadoopYarnNodeManager("HadoopYarnNodeManager"),
    HadoopHdfsDataNode("HadoopHdfsDataNode"),
    HadoopHdfsNameNode("HadoopHdfsNameNode");

    public final String name;

    ServiceName(String name) {
      this.name = name;
    }

    String getName() {
      return this.name;
    }
  }

  static {
    ImmutableMap<String, String> pathMap = new ImmutableMap.Builder<String, String>()
      .put(ServiceName.HBaseRegionServer.getName(), "hbase/hbase-hbase-regionserver.pid")
      .put(ServiceName.HBaseMaster.getName(), "hbase/hbase-hbase-master.pid")
      .put(ServiceName.ZookeeperServer.getName(), "zookeeper/zookeeper-server.pid")
      .put(ServiceName.MySQLServer.getName(), "mysqld/mysqld.pid")
      .put(ServiceName.HiveMetastore.getName(), "hive/hive-metastore.pid")
      .put(ServiceName.HadoopYarnResourceManager.getName(), "hadoop/yarn/yarn-yarn-resourcemanager.pid")
      .put(ServiceName.HadoopYarnNodeManager.getName(), "hadoop/yarn/yarn-yarn-nodemanager.pid")
      .put(ServiceName.HadoopHdfsDataNode.getName(), "hadoop/hdfs/hadoop-hdfs-datanode.pid")
      .put(ServiceName.HadoopHdfsNameNode.getName(), "hadoop/hdfs/hadoop-hdfs-namenode.pid")
      .build();

    Map<ServiceName, Service> services = new HashMap<>();
    for (ServiceName service: ServiceName.values()) {
      services.put(service, new Service(service.getName(), pathMap.get(service.getName())));
    }
    serviceMap = ImmutableMap.copyOf(services);
  }

  public static final ImmutableMap<ServiceName, Service> serviceMap;
  private static final String baseDirectory = "/var/run/";

  private final String name;
  private final File file;

  public Service(String name, String path) {
    this.name = name;
    this.file = new File(baseDirectory, path);
  }

  public File getFile() {
    return this.file;
  }

  public String getName() { return this.name; }
}
