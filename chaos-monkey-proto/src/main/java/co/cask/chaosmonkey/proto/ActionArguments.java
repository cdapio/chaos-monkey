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

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * ActionArguments represents the request body of the action endpoint
 * restartTime and delay Integers are only applicable to Rolling Restart
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
    int setArguments = 0;
    setArguments = nodes == null ? setArguments : setArguments + 1;
    setArguments = count == null ? setArguments : setArguments + 1;
    setArguments = percentage == null ? setArguments : setArguments + 1;
    if (setArguments > 1) {
      throw new IllegalArgumentException("At most one of nodes, count, and percentages can be set");
    }

    if (nodes != null && nodes.isEmpty()) {
      throw new IllegalArgumentException("Nodes parameter cannot be empty");
    }
    if (count != null && count <= 0) {
      throw new IllegalArgumentException("count cannot be less than or equal to zero: " + count);
    }
    if (percentage != null && (percentage <= 0 || percentage > 100)) {
      throw new IllegalArgumentException("percentage needs to be between 0 and 100: " + percentage);
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

    public Builder setNodes(Collection<String> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder setRestartTime(Integer restartTime) {
      this.restartTime = restartTime;
      return this;
    }

    public Builder setDelay(Integer delay) {
      this.delay = delay;
      return this;
    }

    public Builder setCount(Integer count) {
      this.count = count;
      return this;
    }

    public Builder setPercentage(Double percentage) {
      this.percentage = percentage;
      return this;
    }

    public ActionArguments build() {
      ActionArguments actionArguments = new ActionArguments(nodes, restartTime, delay, count, percentage);
      actionArguments.validate();
      return actionArguments;
    }
  }
}
