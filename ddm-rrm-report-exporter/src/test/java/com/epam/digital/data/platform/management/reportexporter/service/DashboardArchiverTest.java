/*
 * Copyright 2024 EPAM Systems.
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

package com.epam.digital.data.platform.management.reportexporter.service;

import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.Page;
import com.epam.digital.data.platform.management.reportexporter.model.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DashboardArchiverTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final DashboardArchiver dashboardArchiver = new DashboardArchiver(objectMapper);

  @SneakyThrows
  @Test
  void expectDashboardZipped() {
    // Given
    Page<Query> queries = new Page<>();
    queries.setCount(1);
    queries.setPage(1);
    queries.setPageSize(20);
    queries.setResults(List.of(new Query()));

    var dashboard = new Dashboard();
    dashboard.setId(1);
    dashboard.setSlug("slug");

    // When
    var result = dashboardArchiver.zipDashboard(queries, dashboard);

    // Then
    ZipInputStream zipInputStream = new ZipInputStream(result.getInputStream());
    assertThat(zipInputStream.getNextEntry().getName()).isEqualTo("queries/queries_slug.json");
    assertThat(zipInputStream.getNextEntry().getName()).isEqualTo("dashboard_slug.json");
  }

}
