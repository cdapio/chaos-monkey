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
import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code Router} for ChaosMonkey.
 */
public class Router extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(Router.class);

  private NettyHttpService httpService;
  private ChaosMonkeyService chaosMonkeyService;

  public Router(ChaosMonkeyService chaosMonkeyService) {
    this.chaosMonkeyService = chaosMonkeyService;
  }

  @Override
  protected void startUp() throws Exception {
    LOG.debug("Starting router");

    this.httpService = NettyHttpService.builder()
      .setPort(Constants.Server.PORT)
      .addHttpHandlers(ImmutableList.of(new HttpHandler(chaosMonkeyService)))
      .build();

    this.httpService.startAsync();
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.debug("Stopping router");

    this.httpService.stopAsync();
  }
}
