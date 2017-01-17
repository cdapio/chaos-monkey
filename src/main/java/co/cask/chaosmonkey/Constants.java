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
   * Constants related to {@code Process}.
   */
  public static final class Process {
    public static final String HBASE_REGIONSERVER = "hbase-regionserver";
    public static final String HBASE_MASTER = "hbase-master";
    public static final String ZOOKEEPER_SERVER = "zookeeper-server";
    public static final String MYSQL_SERVER = "mysql-server";
    public static final String HIVE_METASTORE = "hive-metastore";
    public static final String HADOOP_YARN_RESOURCEMANAGER = "hadoop-yarn-resourcemanager";
    public static final String HADOOP_YARN_NODEMANAGER = "hadoop-yarn-nodemanager";
    public static final String HADOOP_HDFS_DATANODE = "hadoop-hdfs-datanode";
    public static final String HADOOP_HDFS_NAMENODE = "hadoop-hdfs-namenode";
  }
}
