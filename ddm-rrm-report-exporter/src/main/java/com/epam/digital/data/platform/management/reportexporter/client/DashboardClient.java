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

package com.epam.digital.data.platform.management.reportexporter.client;

import com.epam.digital.data.platform.management.reportexporter.config.FeignConfig;
import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.Page;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for Redash dashboards API.
 */
@FeignClient(name = "dashboard", url = "${redash.url}", configuration = FeignConfig.class)
public interface DashboardClient {

  @GetMapping("/api/dashboards")
  ResponseEntity<Page<Dashboard>> getDashboards(@RequestParam("page") Integer page, @RequestParam("page_size") Integer pageSize);

  @GetMapping("/api/dashboards/{id}")
  ResponseEntity<Dashboard> getDashboardById(@PathVariable("id") Long id);
}
