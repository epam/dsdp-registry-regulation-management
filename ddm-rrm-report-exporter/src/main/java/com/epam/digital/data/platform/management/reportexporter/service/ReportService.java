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

package com.epam.digital.data.platform.management.reportexporter.service;

import com.epam.digital.data.platform.management.reportexporter.client.DashboardClient;
import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.dto.DashboardArchiveDto;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.epam.digital.data.platform.management.reportexporter.util.QueryFormatter.formatQueryList;
import static com.epam.digital.data.platform.management.reportexporter.util.RedashResponseHandler.handleResponse;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  private final DashboardClient dashboardClient;
  private final int BATCH_SIZE = 100;
  private final DashboardArchiver archiver;
  private final QueryHelper queryHelper;

  public List<Dashboard> getDashboards() {
    log.info("Retrieving all dashboards");
    int currentPage = 1;
    var allDashboards = new ArrayList<Dashboard>();
    List<Dashboard> dashboards;
    do {
      log.info("Retrieving dashboards page: " + currentPage);
      dashboards = handleResponse(dashboardClient.getDashboards(currentPage, BATCH_SIZE)).getResults();
      allDashboards.addAll(dashboards);
      currentPage++;
      log.info("Retrieved dashboards: " + dashboards.size());
    } while (dashboards.size() == BATCH_SIZE);

    return allDashboards.stream()
        .filter(dashboard -> !dashboard.isDraft())
        .collect(toList());
  }

  public DashboardArchiveDto getArchive(Long id) {
    log.info("Getting dashboard archive by id {}", id);
    var dashboard = handleResponse(dashboardClient.getDashboardById(id));
    var queries = queryHelper.getUtilQueries(dashboard);

    var archive = archiver.zipDashboard(formatQueryList(queries), dashboard);
    return new DashboardArchiveDto(dashboard.getSlug(), archive);
  }

}
