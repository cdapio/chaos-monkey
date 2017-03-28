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

package co.cask.chaosmonkey.proto;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import javax.ws.rs.BadRequestException;

/**
 * ActionArguments represents the request body of the action endpoint
 */
public class ActionArguments {
  private Collection<String> nodes;
  private Integer restartTime;
  private Integer delay;
  private Integer count;
  private Double percentage;

  public ActionArguments() {
    // NO-OP
  }

  private ActionArguments(@Nullable Collection<String> nodes, @Nullable Integer restartTime, @Nullable Integer delay,
                          @Nullable Integer count, @Nullable Double percentage) {
    this.nodes = nodes;
    this.restartTime = restartTime;
    this.delay = delay;
    this.count = count;
    this.percentage = percentage;
  }

  public void validate() {
    if (nodes != null && nodes.isEmpty()) {
      throw new BadRequestException("Nodes parameter cannot be empty");
    }
    if (count != null && count <= 0) {
      throw new BadRequestException("count cannot be less than or equal to zero: " + count);
    }
    if (percentage != null && (percentage <= 0 || percentage > 100)) {
      throw new BadRequestException("percentage needs to be between 0 and 100: " + percentage);
    }
  }

  @Nullable
  public Integer getRestartTime() {
    return restartTime;
  }

  @Nullable
  public Integer getDelay() {
    return delay;
  }

  @Nullable
  public Collection<String> getNodes() {
    return nodes;
  }

  @Nullable
  public Integer getCount() {
    return count;
  }

  @Nullable
  public Double getPercentage() {
    return percentage;
  }

  /**
   * Builder for {@link ActionArguments}
   */
  public static final class Builder {
    private Collection<String> nodes;
    private Integer restartTime;
    private Integer delay;
    private Integer count;
    private Double percentage;

    public Builder addNodes(Collection<String> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder addRestartTime(Integer restartTime) {
      this.restartTime = restartTime;
      return this;
    }

    public Builder addDelay(Integer delay) {
      this.delay = delay;
      return this;
    }

    public Builder addCount(Integer count) {
      this.count = count;
      return this;
    }

    public Builder addPercentage(Double percentage) {
      this.percentage = percentage;
      return this;
    }

    public ActionArguments build() {
      return new ActionArguments(nodes, restartTime, delay, count, percentage);
    }
  }
}
