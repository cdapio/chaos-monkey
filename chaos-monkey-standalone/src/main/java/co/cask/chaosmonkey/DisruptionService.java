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

import co.cask.chaosmonkey.proto.Action;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.SettableFuture;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

/**
 * Service to keep track of running disruptions
 */
public class DisruptionService extends AbstractIdleService {

  private static final ExecutorService executor = Executors.newFixedThreadPool(1);
  private static final Start start = new Start();
  private static final Stop stop = new Stop();
  private static final Kill kill = new Kill();
  private static final Restart restart = new Restart();
  private static final Terminate termiante = new Terminate();

  private Table<String, String, AtomicBoolean> status;

  public DisruptionService(Set<String> services) {
    status = HashBasedTable.create();
    for (String service : services) {
      for (Action action : Action.values()) {
        status.put(service, action.getCommand(), new AtomicBoolean(false));
      }
    }
  }

  public boolean isRunning(String service, String action) {
    if (status.get(service, action) == null) {
      return false;
    }
    return status.get(service, action).get();
  }

  /**
   * Starts a disruption on given set of processes
   *
   * @param action The disruption action
   * @param service The name of the service to be disrupted
   * @param processes Collection of {@link RemoteProcess} to be disrupted
   * @param restartTime Optional, number of seconds a service is down before restarting
   * @param delay Optional, number of seconds between restarting service on different nodes
   * @return {@link Future<Void>} to signal when the disruption is complete
   * @throws IllegalStateException if the same disruption is already running
   */
  public Future<Void> disrupt(Action action, String service, Collection<RemoteProcess> processes,
                              @Nullable Integer restartTime, @Nullable Integer delay) {
    SettableFuture<Void> future = SettableFuture.create();
    if (!checkAndStart(service, action.getCommand())) {
      throw new IllegalStateException(String.format("%s %s is already running", service, action));
    }
    executor.submit(new DisruptionCallable(action, service, processes, status, restartTime, delay, future));
    return future;
  }

  private boolean checkAndStart(String service, String action) {
    AtomicBoolean atomicBoolean = status.get(service, action);
    return atomicBoolean.compareAndSet(false, true);
  }

  @Override
  protected void startUp() throws Exception {
    // NO-OP
  }

  @Override
  protected void shutDown() throws Exception {
    executor.shutdown();
  }

  private static class DisruptionCallable implements Callable<Void> {
    private final Action action;
    private final String service;
    private final Collection<RemoteProcess> processes;
    private final RollingRestart rollingRestart;
    private final Table<String, String, AtomicBoolean> status;
    private final Integer restartTime;
    private final Integer delay;
    private final SettableFuture<Void> future;

    DisruptionCallable(Action action, String service,  Collection<RemoteProcess> processes,
                       Table<String, String, AtomicBoolean> status, @Nullable Integer restartTime,
                       @Nullable Integer delay, SettableFuture<Void> future) {
      this.action = action;
      this.service = service;
      this.processes = processes;
      this.rollingRestart = new RollingRestart();
      this.status = status;
      this.restartTime = restartTime;
      this.delay = delay;
      this.future = future;
    }

    @Override
    public Void call() throws Exception {
      try {
        switch (action) {
          case STOP:
            stop.disrupt(processes);
            break;
          case KILL:
            kill.disrupt(processes);
            break;
          case TERMINATE:
            termiante.disrupt(processes);
            break;
          case START:
            start.disrupt(processes);
            break;
          case RESTART:
            restart.disrupt(processes);
            break;
          case ROLLING_RESTART:
            rollingRestart.disrupt(processes, restartTime, delay);
        }
      } finally {
        release(service, action.getCommand());
        future.set(null);
      }
      return null;
    }

    private void release(String service, String action) {
      AtomicBoolean atomicBoolean = status.get(service, action);
      atomicBoolean.set(false);
    }
  }
}
