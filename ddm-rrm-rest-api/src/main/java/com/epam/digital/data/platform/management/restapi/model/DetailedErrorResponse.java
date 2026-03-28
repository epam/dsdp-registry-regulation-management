/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.management.restapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedErrorResponse {
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Request identifier")
  private String traceId;
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Error code")
  private String code;
  @Schema(requiredMode = RequiredMode.REQUIRED, description = "Error details")
  private String details;
  @Schema(description = "Message key for client localization")
  private String messageKey;
  @Schema(description = "Message parameters for resolving placeholders during client localization")
  private Map<String, String> messageParameters;

  @JsonInclude(Include.NON_NULL)
  public String getDetails() {
    return details;
  }

  @JsonInclude(Include.NON_NULL)
  public String getMessageKey() {
    return messageKey;
  }
}
