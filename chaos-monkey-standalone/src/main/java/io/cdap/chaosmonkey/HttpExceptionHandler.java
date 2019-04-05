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

import com.google.gson.JsonSyntaxException;
import io.cdap.http.ExceptionHandler;
import io.cdap.http.HttpResponder;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

/**
 * Handle exceptions thrown in http handler
 */
public class HttpExceptionHandler extends ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(HttpExceptionHandler.class);

  @Override
  public void handle(Throwable t, HttpRequest request, HttpResponder responder) {
    if (t instanceof BadRequestException) {
      logWithTrace(request, t);
      responder.sendString(HttpResponseStatus.BAD_REQUEST, t.getMessage());
      return;
    }
    if (t instanceof NotFoundException) {
      logWithTrace(request, t);
      responder.sendString(HttpResponseStatus.NOT_FOUND, t.getMessage());
      return;
    }
    if (t instanceof IllegalStateException) {
      logWithTrace(request, t);
      responder.sendString(HttpResponseStatus.CONFLICT, t.getMessage());
      return;
    }
    if (t instanceof JsonSyntaxException) {
      logWithTrace(request, t);
      responder.sendString(HttpResponseStatus.BAD_REQUEST, t.getMessage());
      return;
    }

    LOG.error("Unexpected error: request={} {}:", request.method().name(), request.uri(), t);
    responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, t.getMessage());
  }

  private void logWithTrace(HttpRequest request, Throwable t) {
    LOG.trace("Error in handling request={} {}:", request.method().name(), request.uri(), t);
  }
}
