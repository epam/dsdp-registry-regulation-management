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

package com.epam.digital.data.platform.management.restapi.controller;

import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.service.ReportService;
import com.epam.digital.data.platform.management.restapi.model.DetailedErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Tag(name = "Redash Admin Reports Downloading", description = "redash-admin-dashboard-load-api")
@RestController
@RequestMapping("/reports")
public class RedashReportsController {

  private static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
  private static final String ATTACHMENT_HEADER_VALUE = "attachment; filename=\"dashboard_%s.zip\"";

  private final ReportService service;

  @Operation(
      summary = "Get all redash dashboards for registry admin",
      description =
          "### This endpoint is used as a proxy to Redash API to get all redash dashboards for registry admin.",
      parameters =
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK. List of dashboards returned",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = Dashboard.class)),
                    examples = {
                      @ExampleObject(
                          value =
                              """
                                                            {
                                                              "id": 1,
                                                              "name": "Dashboard 1",
                                                              "slug": "dashboard-1",
                                                              "createdAt": "2021-08-01T12:00:00",
                                                              "updatedAt": "2021-08-01T12:00:00",
                                                              "tags": ["tag1", "tag2"],
                                                              "widgets": [
                                                                {
                                                                  "id": 1,
                                                                  "width": 1,
                                                                  "options": {
                                                                    "text": "Widget 1"
                                                                  }
                                                                }
                                                              ],
                                                              "is_draft": false
                                                            }
                                                    """)
                    })),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DetailedErrorResponse.class)))
      })
  @GetMapping
  public ResponseEntity<List<Dashboard>> getAllDashboards() {
    return ResponseEntity.ok().body(service.getDashboards());
  }

  @Operation(
      summary = "Download a single Redash dashboard by id",
      description =
          "### This endpoint is used to get a single Redash dashboard and load it as an archive",
      parameters =
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")),
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK. Dashboard is downloaded",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
      })
  @GetMapping(path = "/{id}")
  public ResponseEntity<Resource> getDashboardArchive(@PathVariable("id") Long id) {
    var dashboardArchive = service.getArchive(id);

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .contentLength(dashboardArchive.archive().getByteArray().length)
        .header(
            CONTENT_DISPOSITION_HEADER_NAME,
            String.format(ATTACHMENT_HEADER_VALUE, dashboardArchive.dashboardSlug()))
        .body(dashboardArchive.archive());
  }
}
