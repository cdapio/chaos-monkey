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

import com.google.common.util.concurrent.AbstractIdleService;
import io.cdap.chaosmonkey.common.Constants;
import io.cdap.http.NettyHttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code ChaosMonkeyHttpService} for ScheduledDisruption.
 */
public class ChaosMonkeyHttpService extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(ChaosMonkeyHttpService.class);

  private NettyHttpService httpService;
  private ChaosMonkeyService chaosMonkeyService;

  public ChaosMonkeyHttpService(ChaosMonkeyService chaosMonkeyService) {
    this.chaosMonkeyService = chaosMonkeyService;
  }

  @Override
  protected void startUp() throws Exception {
    LOG.debug("Starting ChaosMonkey server");

    this.httpService = NettyHttpService.builder(ChaosMonkeyHttpService.class.getSimpleName())
      .setPort(Constants.Server.PORT)
      .setHttpHandlers(new HttpHandler(chaosMonkeyService))
      .setExceptionHandler(new HttpExceptionHandler())
      .build();

    this.httpService.start();
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.debug("Stopping router");

    this.httpService.stop();
  }
}
