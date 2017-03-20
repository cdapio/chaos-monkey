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

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * ActionArguments represents the request body of the action endpoint
 */
public class ActionArguments {
  private Collection<String> nodes;
  private Integer restartTime;
  private Integer delay;
  private Integer count;
  private Double percentage;

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
}
