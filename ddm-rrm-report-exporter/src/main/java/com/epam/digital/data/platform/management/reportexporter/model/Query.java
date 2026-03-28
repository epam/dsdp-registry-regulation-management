/*
 * Copyright 2021 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.management.reportexporter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Query {
  private Integer id;
  private Integer dataSourceId;
  @EqualsAndHashCode.Include
  private String name;
  @EqualsAndHashCode.Include
  private String query;
  @EqualsAndHashCode.Include
  private String description;
  @JsonInclude(Include.NON_EMPTY)
  private Map<String, Object> schedule;
  private Map<String, Object> options;
  @JsonProperty(access = Access.WRITE_ONLY)
  private List<Visualization> visualizations;
  private boolean isDraft;
}
