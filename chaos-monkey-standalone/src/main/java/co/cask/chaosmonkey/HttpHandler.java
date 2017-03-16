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
import co.cask.chaosmonkey.common.conf.Configuration;
import co.cask.chaosmonkey.proto.ActionStatus;
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import co.cask.chaosmonkey.proto.NodeStatus;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

  private final Configuration conf;
  private final ClusterInfoCollector clusterInfoCollector;
  private final Multimap<String, RemoteProcess> ipToProcess;
  private final Multimap<String, RemoteProcess> nameToProcess;
  private final DisruptionService disruptionService;

  HttpHandler(Configuration conf, ClusterInfoCollector clusterInfoCollector,
              Multimap<String, RemoteProcess> ipToProcess, Multimap<String, RemoteProcess> nameToProcess) {
    this.conf = conf;
    this.clusterInfoCollector = clusterInfoCollector;
    this.ipToProcess = ipToProcess;
    this.nameToProcess = nameToProcess;
    this.disruptionService = new DisruptionService(nameToProcess.keySet());
  }

  @POST
  @Path("/services/{service}/{action}")
  public void executeAction(HttpRequest request, HttpResponder responder,
                            @PathParam("service") String service, @PathParam("action") String action) throws Exception {
    Collection<RemoteProcess> processes = nameToProcess.get(service);
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

    ActionArguments actionArguments;
    try (Reader reader = new InputStreamReader(new ChannelBufferInputStream(request.getContent()), Charsets.UTF_8)) {
      actionArguments = GSON.fromJson(reader, ActionArguments.class);
    } catch (JsonSyntaxException e) {
      responder.sendString(HttpResponseStatus.BAD_REQUEST, "Invalid request body");
      return;
    }

    disruptionService.disrupt(actionEnum, service, processes, actionArguments, responder);
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
    Collection<RemoteProcess> remoteProcessList = ipToProcess.get(ip);
    if (remoteProcessList.size() == 0) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, "Unknown ip: " + ip);
      return;
    }
    NodeStatus status = new NodeStatus(ip);
    for (RemoteProcess remoteProcess : remoteProcessList) {
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
    List<NodeStatus> statuses = new ArrayList<>();
    for (String ip : ipToProcess.keySet()) {
      Collection<RemoteProcess> remoteProcessList = ipToProcess.get(ip);
      NodeStatus status = new NodeStatus(ip);
      for (RemoteProcess remoteProcess : remoteProcessList) {
        status.serviceStatus.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
      }
      statuses.add(status);
    }
    responder.sendJson(HttpResponseStatus.OK, statuses);
  }
}
