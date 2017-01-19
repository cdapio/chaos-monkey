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
import java.io.IOException;

/**
 * A process that the chaos monkey can interact with. All supported processes are stored on the PROCESS_MAP
 */
public class Process {

  static {
    PROCESS_MAP = new ImmutableMap.Builder<String, Process>()
      .put(Constants.Process.HBASE_REGIONSERVER,
           new Process(Constants.Process.HBASE_REGIONSERVER, "hbase/hbase-hbase-regionserver.pid"))
      .put(Constants.Process.HBASE_MASTER,
           new Process(Constants.Process.HBASE_MASTER, "hbase/hbase-hbase-master.pid"))
      .put(Constants.Process.ZOOKEEPER_SERVER,
           new Process(Constants.Process.ZOOKEEPER_SERVER, "zookeeper/zookeeper-server.pid"))
      .put(Constants.Process.MYSQL_SERVER,
           new Process(Constants.Process.MYSQL_SERVER, "mysqld/mysqld.pid"))
      .put(Constants.Process.HIVE_METASTORE,
           new Process(Constants.Process.HIVE_METASTORE, "hive/hive-metastore.pid"))
      .put(Constants.Process.HADOOP_YARN_RESOURCEMANAGER,
           new Process(Constants.Process.HADOOP_YARN_RESOURCEMANAGER, "hadoop/yarn/yarn-yarn-resourcemanager.pid"))
      .put(Constants.Process.HADOOP_YARN_NODEMANAGER,
           new Process(Constants.Process.HADOOP_YARN_NODEMANAGER, "hadoop/yarn/yarn-yarn-nodemanager.pid"))
      .put(Constants.Process.HADOOP_HDFS_DATANODE,
           new Process(Constants.Process.HADOOP_HDFS_DATANODE, "hadoop/hdfs/hadoop-hdfs-datanode.pid"))
      .put(Constants.Process.HADOOP_HDFS_NAMENODE,
           new Process(Constants.Process.HADOOP_HDFS_NAMENODE, "hadoop/hdfs/hadoop-hdfs-namenode.pid"))
      .build();
  }

  public static final ImmutableMap<String, Process> PROCESS_MAP;
  private static final String baseDirectory = "/var/run/";

  private final String name;
  private final File file;

  /**
   * Creates a new process object with given name and path to PID file
   * @param name name of the process
   * @param path the path of file containing the process ID
   */
  public Process(String name, String path) {
    this.name = name;
    this.file = new File(baseDirectory, path);
  }

  /**
   * Creates a new process object with path to PID file
   * @param path the path of file containing the process ID
   */
  public Process(String path) {
    this(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')), path);
  }

  /**
   * Get the PID file of the process
   * @return File contaning the PID
   */
  public File getFile() {
    return this.file;
  }

  /**
   * Get the name of the process
   * @return String name of process
   */
  public String getName() {
    return this.name;
  }

  /**
   * Kills this process
   * @return Exit code
   * @throws IOException
   */
  public int kill() throws IOException {
    ProcessHandler handler = new ProcessHandler(new Shell());
    return handler.killProcess(this);
  }

  /**
   * Stops this process
   * @return Exit code
   * @throws IOException
   */
  public int stop() throws IOException {
    ProcessHandler handler = new ProcessHandler(new Shell());
    return handler.stopProcess(this);
  }

  /**
   * Sends a signal to this process
   * @param signal The UNIX signal to send to this process
   * @return Exit code
   * @throws IOException
   */
  public int signal(int signal) throws IOException {
    ProcessHandler handler = new ProcessHandler(new Shell());
    return handler.signalProcess(signal, this);
  }
}
