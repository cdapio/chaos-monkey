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
import co.cask.chaosmonkey.proto.NodeStatus;
import co.cask.http.AbstractHttpHandler;
import co.cask.http.HttpResponder;
import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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

  private final ChaosMonkeyService chaosMonkeyService;

  HttpHandler(ChaosMonkeyService chaosMonkeyService) {
    this.chaosMonkeyService = chaosMonkeyService;
  }

  @POST
  @Path("/services/{service}/{action}")
  public void executeAction(HttpRequest request, HttpResponder responder,
                            @PathParam("service") String service, @PathParam("action") String action) throws Exception {
    ActionArguments actionArguments;
    try (Reader reader = new InputStreamReader(new ChannelBufferInputStream(request.getContent()), Charsets.UTF_8)) {
      actionArguments = GSON.fromJson(reader, ActionArguments.class);
    } catch (JsonSyntaxException e) {
      responder.sendString(HttpResponseStatus.BAD_REQUEST, "Invalid request body: " + e.getMessage());
      return;
    }

    try {
      if (actionArguments == null) {
        chaosMonkeyService.executeAction(service, action, null, null, null, null, null);
      } else {
        chaosMonkeyService.executeAction(service, action, actionArguments.getNodes(), actionArguments.getCount(),
                                         actionArguments.getPercentage(), actionArguments.getRestartTime(),
                                         actionArguments.getDelay());
      }
    } catch (BadRequestException e) {
      responder.sendString(HttpResponseStatus.BAD_REQUEST, e.getMessage());
      return;
    } catch (NotFoundException e) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, e.getMessage());
      return;
    } catch (IllegalStateException e) {
      responder.sendString(HttpResponseStatus.CONFLICT, e.getMessage());
      return;
    }
    responder.sendString(HttpResponseStatus.OK, "success");
  }

  @GET
  @Path("/services/{service}/{action}/status")
  public void getActionStatus(HttpRequest request, HttpResponder responder,
                                      @PathParam("service") String service,
                                      @PathParam("action") String action) throws Exception {
    responder.sendJson(HttpResponseStatus.OK, chaosMonkeyService.getActionStatus(service, action));
  }

  /**
   * Gets the status of services managed by chaos monkey on the given ip address
   */
  @GET
  @Path("/nodes/{ip}/status")
  public void getNodeStatus(HttpRequest request, HttpResponder responder, @PathParam("ip") String ip) throws Exception {
    NodeStatus status;
    try {
      status = chaosMonkeyService.getNodeStatus(ip);
    } catch (NotFoundException e) {
      responder.sendString(HttpResponseStatus.NOT_FOUND, e.getMessage());
      return;
    } catch (Exception e) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    }
    responder.sendJson(HttpResponseStatus.OK, status);
  }

  /**
   * Gets the status of all services managed by chaos monkey
   */
  @GET
  @Path("/status")
  public void getNodeStatuses(HttpRequest request, HttpResponder responder) throws Exception {
    Collection<NodeStatus> statuses;
    try {
      statuses = chaosMonkeyService.getNodeStatuses();
    } catch (Exception e) {
      responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage());
      return;
    }
    responder.sendJson(HttpResponseStatus.OK, statuses);
  }
}
