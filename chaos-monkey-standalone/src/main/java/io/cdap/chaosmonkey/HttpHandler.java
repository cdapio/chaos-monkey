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

package io.cdap.chaosmonkey;

import com.google.gson.Gson;
import io.cdap.chaosmonkey.common.Constants;
import io.cdap.chaosmonkey.proto.ActionArguments;
import io.cdap.http.AbstractHttpHandler;
import io.cdap.http.HttpResponder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.charset.StandardCharsets;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * The class that handles HTTP calls.
 */
@Path(Constants.Server.API_VERSION_1)
public class HttpHandler extends AbstractHttpHandler {

  private static final Gson GSON = new Gson();

  private final ChaosMonkeyService chaosMonkeyService;

  HttpHandler(ChaosMonkeyService chaosMonkeyService) {
    this.chaosMonkeyService = chaosMonkeyService;
  }

  @POST
  @Path("/services/{service}/{action}")
  public void executeAction(FullHttpRequest request, HttpResponder responder,
                            @PathParam("service") String service, @PathParam("action") String action) {
    ActionArguments actionArguments = GSON.fromJson(request.content().toString(StandardCharsets.UTF_8),
                                                    ActionArguments.class);

    chaosMonkeyService.executeAction(service, action, actionArguments);
    responder.sendString(HttpResponseStatus.OK, "success");
  }

  @GET
  @Path("/services/{service}/{action}/status")
  public void getActionStatus(HttpRequest request, HttpResponder responder,
                                      @PathParam("service") String service,
                                      @PathParam("action") String action) {
    responder.sendJson(HttpResponseStatus.OK, GSON.toJson(chaosMonkeyService.getActionStatus(service, action)));
  }

  /**
   * Gets the status of services managed by chaos monkey on the given ip address
   */
  @GET
  @Path("/nodes/{ip}/status")
  public void getNodeStatus(HttpRequest request, HttpResponder responder, @PathParam("ip") String ip) throws Exception {
    responder.sendJson(HttpResponseStatus.OK, GSON.toJson(chaosMonkeyService.getNodeStatus(ip)));
  }

  /**
   * Gets the status of all services managed by chaos monkey
   */
  @GET
  @Path("/status")
  public void getNodeStatuses(HttpRequest request, HttpResponder responder) throws Exception {
    responder.sendJson(HttpResponseStatus.OK, GSON.toJson(chaosMonkeyService.getNodeStatuses()));
  }

  /**
   * Gets the disruptions available for each service
   */
  @GET
  @Path("/services")
  public void getServices(HttpRequest request, HttpResponder responder) throws Exception {
    responder.sendJson(HttpResponseStatus.OK, GSON.toJson(chaosMonkeyService.getServices()));
  }
}
