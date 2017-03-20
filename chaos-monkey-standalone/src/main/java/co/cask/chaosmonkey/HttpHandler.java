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

import co.cask.chaosmonkey.common.Constants;
import co.cask.chaosmonkey.proto.ActionStatus;
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import co.cask.chaosmonkey.proto.NodeStatus;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.common.collect.Table;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.jersey.api.ConflictException;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * The class that handles HTTP calls.
 */
@Path(Constants.Server.API_VERSION_1)
public class HttpHandler extends AbstractHttpHandler {
  private static final Logger LOG = LoggerFactory.getLogger(HttpHandler.class);
  private static final Gson GSON = new Gson();

  private final DisruptionService disruptionService;
  private final Table<String, String, RemoteProcess> processTable;
  private final ExecutorService executor;

  HttpHandler(Table<String, String, RemoteProcess> processTable) {
    this.disruptionService = new DisruptionService(processTable.columnKeySet());
    this.processTable = processTable;
    this.executor = Executors.newFixedThreadPool(processTable.rowKeySet().size());
  }

  @POST
  @Path("/services/{service}/{action}")
  public void executeAction(HttpRequest request, HttpResponder responder,
                            @PathParam("service") String service, @PathParam("action") String action) throws Exception {
    ActionArguments actionArguments;
    Collection<RemoteProcess> processes = processTable.column(service).values();
    try (Reader reader = new InputStreamReader(new ChannelBufferInputStream(request.getContent()), Charsets.UTF_8)) {
      actionArguments = GSON.fromJson(reader, ActionArguments.class);
    } catch (JsonSyntaxException e) {
      responder.sendString(HttpResponseStatus.BAD_REQUEST, "Invalid request body: " + e.getMessage());
      return;
    }

    if (actionArguments == null) {
      // NO OP
    } else if (actionArguments.getNodes() != null) {
      processes = new HashSet<>();
      List<String> invalidNodes = new ArrayList<>();
      for (String nodeIp : actionArguments.getNodes()) {
        RemoteProcess process = processTable.get(nodeIp, service);
        if (process == null) {
          invalidNodes.add(nodeIp);
        } else {
          processes.add(process);
        }
      }
      if (!invalidNodes.isEmpty()) {
        responder.sendString(HttpResponseStatus.BAD_REQUEST, "The following nodes do not exist, or they do not " +
          "support " + service + ": " + invalidNodes);
        return;
      }
    } else if (actionArguments.getCount() != null) {
      int count = actionArguments.getCount();
      if (count <= 0) {
        responder.sendString(HttpResponseStatus.BAD_REQUEST, "count cannot be less than or equal to zero: " + count);
      }
      List<RemoteProcess> processList = new ArrayList<>(processTable.column(service).values());
      Collections.shuffle(processList);
      processes = new HashSet<>(processList.subList(0, Math.min(processList.size(), count)));
    } else if (actionArguments.getPercentage() != null) {
      double percentage = actionArguments.getPercentage();
      if (percentage <= 0 || percentage > 100) {
        responder.sendString(HttpResponseStatus.BAD_REQUEST, "percentage needs to be between 0 and 100: " + percentage);
      }
      List<RemoteProcess> processList = new ArrayList<>(processTable.column(service).values());
      Collections.shuffle(processList);
      processes = new HashSet<>(processList.subList(0, (int) Math.round(processList.size() *
                                                                          (actionArguments.getPercentage() / 100))));
    }

    if (processes.size() == 0) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Unknown service: " + service);
      return;
    }

    Action actionEnum;
    try {
      actionEnum = Action.valueOf(action.toUpperCase().replace('-', '_'));
    } catch (IllegalArgumentException e) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Unknown action: " + action);
      return;
    }

    try {
      disruptionService.disrupt(actionEnum, service, processes, actionArguments);
    } catch (ConflictException e) {
      responder.sendString(HttpResponseStatus.CONFLICT, String.format("Conflict: %s %s is already running",
                                                                      service, action));
      return;
    }
    responder.sendString(HttpResponseStatus.OK, "success");
  }

  @GET
  @Path("/services/{service}/{action}/status")
  public void getRollingRestartStatus(HttpRequest request, HttpResponder responder,
                                      @PathParam("service") String service,
                                      @PathParam("action") String action) throws Exception {
    responder.sendJson(HttpResponseStatus.OK,
                       new ActionStatus(service, action, disruptionService.isRunning(service, action)));
  }

  /**
   * Gets the status of services managed by chaos monkey on the given ip address
   */
  @GET
  @Path("/nodes/{ip}/status")
  public void getNodeStatus(HttpRequest request, HttpResponder responder, @PathParam("ip") String ip) throws Exception {
    Collection<RemoteProcess> remoteProcesses = processTable.row(ip).values();
    if (remoteProcesses.isEmpty()) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Unknown ip: " + ip);
      return;
    }
    NodeStatus status = new NodeStatus(ip);
    for (RemoteProcess remoteProcess : remoteProcesses) {
      status.serviceStatus.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
    }
    responder.sendJson(HttpResponseStatus.OK, status);
  }

  /**
   * Gets the status of all services managed by chaos monkey
   */
  @GET
  @Path("/status")
  public void getNodeStatuses(HttpRequest request, HttpResponder responder) throws Exception {
    List<Status> threads = new ArrayList<>();
    for (String ip : processTable.rowKeySet()) {
      threads.add(new Status(ip, processTable.row(ip).values()));
    }
    List<Future<NodeStatus>> results = executor.invokeAll(threads);

    List<NodeStatus> statuses = new ArrayList<>();
    for (Future<NodeStatus> result : results) {
      statuses.add(result.get());
    }
    responder.sendJson(HttpResponseStatus.OK, statuses);
  }

  private static class Status implements Callable<NodeStatus> {
    private final Collection<RemoteProcess> processes;
    private final String ip;

    Status(String ip, Collection<RemoteProcess> processes) {
      this.ip = ip;
      this.processes = processes;
    }

    @Override
    public NodeStatus call() throws Exception {
      NodeStatus status = new NodeStatus(ip);
      for (RemoteProcess remoteProcess : processes) {
        status.serviceStatus.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
      }
      return status;
    }
  }
}
