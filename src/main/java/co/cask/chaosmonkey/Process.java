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
import java.util.Scanner;

/**
 * A process that the chaos monkey can interact with. All supported processes are stored on the PROCESS_MAP
 */
public class Process {

  static {
    PROCESS_MAP = new ImmutableMap.Builder<String, Process>()
      .put(Constants.Process.HBASE_REGIONSERVER,
           new Process(Constants.Process.HBASE_REGIONSERVER,
                       "hbase/hbase-hbase-regionserver.pid",
                       "hbase-regionserver"))
      .put(Constants.Process.HBASE_MASTER,
           new Process(Constants.Process.HBASE_MASTER,
                       "hbase/hbase-hbase-master.pid",
                       "hbase-master"))
      .put(Constants.Process.ZOOKEEPER_SERVER,
           new Process(Constants.Process.ZOOKEEPER_SERVER,
                       "zookeeper/zookeeper-server.pid",
                       "zookeeper-server"))
      .put(Constants.Process.MYSQL_SERVER,
           new Process(Constants.Process.MYSQL_SERVER,
                       "mysqld/mysqld.pid",
                       "mysqld"))
      .put(Constants.Process.HIVE_METASTORE,
           new Process(Constants.Process.HIVE_METASTORE,
                       "hive/hive-metastore.pid",
                       "hive-metastore"))
      .put(Constants.Process.HADOOP_YARN_RESOURCEMANAGER,
           new Process(Constants.Process.HADOOP_YARN_RESOURCEMANAGER,
                       "hadoop/yarn/yarn-yarn-resourcemanager.pid",
                       "hadoop-yarn-resourcemanager"))
      .put(Constants.Process.HADOOP_YARN_NODEMANAGER,
           new Process(Constants.Process.HADOOP_YARN_NODEMANAGER,
                       "hadoop/yarn/yarn-yarn-nodemanager.pid",
                       "hadoop-yarn-nodemanager"))
      .put(Constants.Process.HADOOP_HDFS_DATANODE,
           new Process(Constants.Process.HADOOP_HDFS_DATANODE,
                       "hadoop/hdfs/hadoop-hdfs-datanode.pid",
                       "hadoop-hdfs-datanode"))
      .put(Constants.Process.HADOOP_HDFS_NAMENODE,
           new Process(Constants.Process.HADOOP_HDFS_NAMENODE,
                       "hadoop/hdfs/hadoop-hdfs-namenode.pid",
                       "hadoop-hdfs-namenode"))
      .build();
  }

  public static final ImmutableMap<String, Process> PROCESS_MAP;
  private static final String pidBaseDirectory = "/var/run/";
  private static final String initScriptDirectory = "/etc/init.d/";

  private final String name;
  private final File pidFile;
  private final File initScript;

  /**
   * Creates a new process object with given name and path to PID file
   * @param name name of the process
   * @param pidPath the path of file containing the process ID
   * @param initPath the path of the init script
   */
  public Process(String name, String pidPath, String initPath) {
    this.name = name;
    this.pidFile = new File(pidBaseDirectory, pidPath);
    this.initScript = new File(initScriptDirectory, initPath);
  }

  /**
   * Get the PID file of the process
   * @return File contaning the PID
   */
  public File getPidFile() {
    return this.pidFile;
  }

  /**
   * Get the init script for this process
   * @return init script
   */
  public File getInitScript() {
    return this.initScript;
  }

  /**
   * Get the name of the process
   * @return String name of process
   */
  public String getName() {
    return this.name;
  }

  /**
   * Checks whether this process is currently running
   * @return True if running, false if not running
   */
  public boolean isRunning() {
    if (this.getPidFile().exists() && this.getInitScript().canRead()) {
      try (Scanner scanner = new Scanner(this.getPidFile())) {
        if (scanner.nextInt() >= 0) {
          return true;
        }
      } catch (Exception e) {
        return false;
      }
    }
    return false;
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
