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

import java.io.File;

/**
 * TODO: procrastinate on documentation
 */
public enum Service {
  HBaseRegionServer("hbase/hbase-hbase-regionserver.pid"),
  HBaseMaster("hbase/hbase-hbase-master.pid"),
  ZookeeperServer("zookeeper/zookeeper-server.pid"),
  MySQLServer("mysqld/mysqld.pid"),
  HiveMetastore("hive/hive-metastore.pid"),
  HadoopYarnResourceManager("hadoop/yarn/yarn-yarn-resourcemanager.pid"),
  HadoopYarnNodeManager("hadoop/yarn/yarn-yarn-nodemanager.pid"),
  HadoopHdfsDataNode("hadoop/hdfs/hadoop-hdfs-datanode.pid"),
  HadoopHdfsNameNode("hadoop/hdfs/hadoop-hdfs-namenode.pid");

  public static final String baseDirectory = "/var/run/";

  private final File file;

  Service(String path) {
    this.file = new File(baseDirectory, path);
  }

  public File getFile() {
    return this.file;
  }
}
