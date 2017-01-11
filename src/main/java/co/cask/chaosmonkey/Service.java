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

/**
 * TODO: procrastinate on documentation
 */
public class Service {

  public static final ImmutableMap<String, Service> commonServices = new ImmutableMap.Builder<String, Service>()
    .put("HBaseRegionServer", new Service("hbase/hbase-hbase-regionserver.pid"))
    .put("HBaseMaster", new Service("hbase/hbase-hbase-master.pid"))
    .put("ZookeeperServer", new Service("zookeeper/zookeeper-server.pid"))
    .put("MySQLServer", new Service("mysqld/mysqld.pid"))
    .put("HiveMetastore", new Service("hive/hive-metastore.pid"))
    .put("HadoopYarnResourceManager", new Service("hadoop/yarn/yarn-yarn-resourcemanager.pid"))
    .put("HadoopYarnNodeManager", new Service("hadoop/yarn/yarn-yarn-nodemanager.pid"))
    .put("HadoopHdfsDataNode", new Service("hadoop/hdfs/hadoop-hdfs-datanode.pid"))
    .put("HadoopHdfsNameNode", new Service("hadoop/hdfs/hadoop-hdfs-namenode.pid"))
    .build();
  public static final String baseDirectory = "/var/run/";

  private final File file;

  public Service(String path) {
    this.file = new File(baseDirectory, path);
  }

  public File getFile() {
    return this.file;
  }
}
