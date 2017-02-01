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

import co.cask.chaosmonkey.conf.Configuration;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multimap;
import com.jcraft.jsch.JSchException;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * The class that handles HTTP calls.
 */
@Path(Constants.Server.API_VERSION_1)
public class HttpHandler extends AbstractHttpHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHandler.class);

  private final Configuration conf;
  private final Multimap<String, RemoteProcess> ipToProcess;
  private static final ObjectMapper mapper = new ObjectMapper();

  HttpHandler(Configuration conf, Multimap<String, RemoteProcess> ipToProcess) {
    this.conf = conf;
    this.ipToProcess = ipToProcess;
  }

  @POST
  @Path("/services/{service}/{action}")
  public void executeAction(HttpRequest request, HttpResponder responder,
                            @PathParam("service") String service, @PathParam("action") String action) throws Exception {
    for (RemoteProcess remoteProcess : ipToProcess.values()) {
      if (remoteProcess.getName().equals(service)) {
        try {
          switch (action) {
            case Constants.RemoteProcess.STOP:
              remoteProcess.stop();
              break;
            case Constants.RemoteProcess.KILL:
              remoteProcess.kill();
              break;
            case Constants.RemoteProcess.TERMINATE:
              remoteProcess.terminate();
              break;
            case Constants.RemoteProcess.START:
              remoteProcess.start();
              break;
            case Constants.RemoteProcess.RESTART:
              remoteProcess.restart();
              break;
            default:
              responder.sendString(HttpResponseStatus.BAD_REQUEST, "Unknown command: " + action);
              return;
          }
        } catch (JSchException e) {
          responder.sendString(HttpResponseStatus.BAD_REQUEST, e.getMessage());
          return;
        }
      }
    }

    responder.sendString(HttpResponseStatus.OK, "success");
  }

  /**
   * Gets node properties of all nodes in the current cluster, whether or not it is managed by the chaos monkey
   */
  @GET
  @Path("/nodes")
  public void getNodes(HttpRequest request, HttpResponder responder) throws Exception {
    Map<String, NodeProperties> nodePropertiesMap = ChaosMonkeyRunner.getNodeProperties(conf);
    String response = mapper.writeValueAsString(nodePropertiesMap.values());
    responder.sendString(HttpResponseStatus.OK, response);
  }

  /**
   * Gets the status of services managed by chaos monkey on the given ip address
   */
  @GET
  @Path("/nodes/{ip}/status")
  public void getNodeStatus(HttpRequest request, HttpResponder responder, @PathParam("ip") String ip) throws Exception {
    Collection<RemoteProcess> remoteProcessList = ipToProcess.get(ip);
    Map<String, String> statuses = new HashMap<>();
    for (RemoteProcess remoteProcess : remoteProcessList) {
      statuses.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
    }
    String response = mapper.writeValueAsString(statuses);
    responder.sendString(HttpResponseStatus.OK, response);
  }

  /**
   * Gets the status of all services managed by chaos monkey
   */
  @GET
  @Path("/status")
  public void getNodeStatuses(HttpRequest request, HttpResponder responder) throws Exception {
    Map<String, Map<String, String>> statuses = new HashMap<>();
    for (String ipAddress : ipToProcess.keySet()) {
      Collection<RemoteProcess> remoteProcessList = ipToProcess.get(ipAddress);
      Map<String, String> status = new HashMap<>();
      for (RemoteProcess remoteProcess : remoteProcessList) {
        status.put(remoteProcess.getName(), remoteProcess.isRunning() ? "running" : "stopped");
      }
      statuses.put(ipAddress, status);
    }
    String response = mapper.writeValueAsString(statuses);
    responder.sendString(HttpResponseStatus.OK, response);
  }
}
