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

import com.epam.digital.data.platform.management.reportexporter.exception.RedashDashboardZippingException;
import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.Page;
import com.epam.digital.data.platform.management.reportexporter.model.Query;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@RequiredArgsConstructor
@Component
public class DashboardArchiver {

  private static final String QUERY_FILE_NAME = "queries/queries_%s.json";
  private static final String DASHBOARD_FILE_NAME = "dashboard_%s.json";

  private final ObjectMapper reportExporterMapper;

  public ByteArrayResource zipDashboard(Page<Query> queries, Dashboard dashboard) {
    log.info("Creating zip archive for dashboard with slug {}", dashboard.getSlug());

    try {
      var zip = zip(queries, dashboard);
      return new ByteArrayResource(zip);
    } catch (Exception e) {
      log.error("Error during converting zip to byte array for dashboard slug {}", dashboard.getSlug());
      throw new RedashDashboardZippingException("Could not zip dashboard", e);
    }
  }

  private byte[] zip(Page<Query> queries, Dashboard dashboard) {
    try (var result = new ByteArrayOutputStream();
         var zipStream = new ZipOutputStream(result)) {
      zipQuery(zipStream, queries, dashboard.getSlug());
      zipDashboard(zipStream, dashboard);
      zipStream.finish();
      return result.toByteArray();
    } catch (Exception e) {
      log.error("Error during creating zip archive for dashboard slug {}", dashboard.getSlug());
      throw new RedashDashboardZippingException("Could not zip dashboard", e);
    }
  }

  private void zipQuery(ZipOutputStream zipStream, Page<Query> queries, String slug)
      throws IOException {
    log.info("Putting query file into zip archive");
    var queryFile = new ZipEntry(String.format(QUERY_FILE_NAME, FilenameUtils.getName(slug)));
    var queryData = reportExporterMapper.writeValueAsString(queries).getBytes(StandardCharsets.UTF_8);

    zipStream.putNextEntry(queryFile);
    zipStream.write(queryData);
    zipStream.closeEntry();
  }

  private void zipDashboard(ZipOutputStream zipStream, Dashboard dashboard)
      throws IOException {
    log.info("Putting dashboard file into zip archive");
    var dashboardFile = new ZipEntry(String.format(DASHBOARD_FILE_NAME, FilenameUtils.getName(dashboard.getSlug())));
    var dashboardData = reportExporterMapper.writeValueAsString(dashboard).getBytes(StandardCharsets.UTF_8);

    zipStream.putNextEntry(dashboardFile);
    zipStream.write(dashboardData);
    zipStream.closeEntry();
  }
}
