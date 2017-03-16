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
import co.cask.chaosmonkey.proto.ClusterInfoCollector;
import co.cask.http.NettyHttpService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AbstractIdleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code Router} for ChaosMonkey.
 */
public class Router extends AbstractIdleService {
  private static final Logger LOG = LoggerFactory.getLogger(Router.class);

  private NettyHttpService httpService;
  private ClusterInfoCollector clusterInfoCollector;
  private final Multimap<String, RemoteProcess> ipToProcess;
  private final Multimap<String, RemoteProcess> nameToProcess;
  private final Configuration conf;

  public Router(Configuration conf, ClusterInfoCollector clusterInfoCollector,
                Multimap<String, RemoteProcess> ipToProcess, Multimap<String, RemoteProcess> nameToProcess) {
    this.conf = conf;
    this.clusterInfoCollector = clusterInfoCollector;
    this.ipToProcess = ipToProcess;
    this.nameToProcess = nameToProcess;
  }

  @Override
  protected void startUp() throws Exception {
    LOG.debug("Starting router");

    this.httpService = NettyHttpService.builder()
      .setPort(Constants.Server.PORT)
      .addHttpHandlers(ImmutableList.of(new HttpHandler(conf, clusterInfoCollector, ipToProcess, nameToProcess)))
      .build();

    this.httpService.startAsync();
  }

  @Override
  protected void shutDown() throws Exception {
    LOG.debug("Stopping router");

    this.httpService.stopAsync();
  }
}
