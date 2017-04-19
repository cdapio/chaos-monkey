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

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.SettableFuture;
import sun.plugin.dom.exception.InvalidStateException;

import java.util.Collection;
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

  private Table<String, String, AtomicBoolean> status;
  private Table<String, String, Disruption> disruptionMap;

  public DisruptionService(Table<String, String, Disruption> compatibleDisruptions) {
    this.disruptionMap = compatibleDisruptions;
    status = HashBasedTable.create();
    for (String service : compatibleDisruptions.rowKeySet()) {
      for (String disruptionName : compatibleDisruptions.columnKeySet()) {
        if (compatibleDisruptions.get(service, disruptionName) != null) {
          status.put(service, disruptionName, new AtomicBoolean(false));
        }
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
   * @param disruptionName The name of the disruption to perform
   * @param service The name of the service to be disrupted
   * @param processes Collection of {@link RemoteProcess} to be disrupted
   * @param restartTime Optional, number of seconds a service is down before restarting
   * @param delay Optional, number of seconds between restarting service on different nodes
   * @return {@link Future<Void>} to signal when the disruption is complete
   * @throws IllegalStateException if the same disruption is already running
   */
  public Future<Void> disrupt(String disruptionName, String service, Collection<RemoteProcess> processes,
                              @Nullable Integer restartTime, @Nullable Integer delay) {
    SettableFuture<Void> future = SettableFuture.create();
    if (!checkAndStart(service, disruptionName)) {
      throw new IllegalStateException(String.format("Conflict: %s %s is already running", service, disruptionName));
    }
    executor.submit(new DisruptionCallable(disruptionMap.get(service, disruptionName), service, processes, status,
                                           restartTime, delay, future));
    return future;
  }

  private boolean checkAndStart(String service, String action) {
    AtomicBoolean atomicBoolean = status.get(service, action);
    if (atomicBoolean == null) {
      throw new InvalidStateException(String.format("%s is not a valid action on %s", action, service));
    }
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
    private final Disruption disruption;
    private final String service;
    private final Collection<RemoteProcess> processes;
    private final RollingRestart rollingRestart;
    private final Table<String, String, AtomicBoolean> status;
    private final Integer restartTime;
    private final Integer delay;
    private final SettableFuture<Void> future;

    DisruptionCallable(Disruption disruption, String service,  Collection<RemoteProcess> processes,
                       Table<String, String, AtomicBoolean> status, @Nullable Integer restartTime,
                       @Nullable Integer delay, SettableFuture<Void> future) {
      this.disruption = disruption;
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
        if (disruption.getName().equals(rollingRestart.getName())) {
          rollingRestart.disrupt(processes, restartTime, delay);
        } else {
          disruption.disrupt(processes);
        }
      } finally {
        release(service, disruption.getName());
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
