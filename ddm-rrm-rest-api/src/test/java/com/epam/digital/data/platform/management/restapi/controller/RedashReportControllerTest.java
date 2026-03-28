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
import com.epam.digital.data.platform.management.reportexporter.model.dto.DashboardArchiveDto;
import com.epam.digital.data.platform.management.reportexporter.service.ReportService;
import com.epam.digital.data.platform.management.restapi.exception.ApplicationExceptionHandler;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static java.util.List.of;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerTest({RedashReportsController.class, ApplicationExceptionHandler.class})
@DisplayName("Redash reports controller test")
class RedashReportControllerTest {

  static final String BASE_URL = "/reports";

  static final Long DASHBOARD_ID = 1L;
  static final String DASHBOARD_SLUG_ID = "stub_slug";
  static final String ENCODED_STRING = Base64.getEncoder().encodeToString("test".getBytes());

  static final String HEADER_NAME = "Content-Disposition";
  static final String HEADER_VALUE = "attachment; filename=\"dashboard_stub_slug.zip\"";

  @Autowired
  MockMvc mockMvc;

  @MockBean
  ReportService reportService;

  @MockBean
  Tracer tracer;

  @Test
  void expectSuccessfullyRetrievedDashboardsIfServiceReturned() throws Exception {
    var dashboards = stubDashboards();
    when(reportService.getDashboards()).thenReturn(dashboards);

    mockMvc
        .perform(get(BASE_URL))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_JSON),
            jsonPath("$[0].name", is("stub name")),
            jsonPath("$[0].slug", is("stub_slug")),
            jsonPath("$[0].createdAt", is("2024-04-22T11:51:00.000Z")));
  }

  @Test
  void expectSuccessfullyLoadedZipIfServiceReturned() throws Exception {
    when(reportService.getArchive(any()))
        .thenReturn(
            new DashboardArchiveDto(
                DASHBOARD_SLUG_ID,
                new ByteArrayResource(Base64.getDecoder().decode(ENCODED_STRING))));

    mockMvc
        .perform(get(BASE_URL + "/{id}", DASHBOARD_ID))
        .andExpectAll(
            status().isOk(),
            content().contentType(MediaType.APPLICATION_OCTET_STREAM),
            header().string(HEADER_NAME, HEADER_VALUE));
  }

  private List<Dashboard> stubDashboards() {
    var dashboard = new Dashboard();
    dashboard.setName("stub name");
    dashboard.setSlug("stub_slug");
    dashboard.setCreatedAt(LocalDateTime.of(2024, 4, 22, 11, 51));
    return of(dashboard);
  }
}
