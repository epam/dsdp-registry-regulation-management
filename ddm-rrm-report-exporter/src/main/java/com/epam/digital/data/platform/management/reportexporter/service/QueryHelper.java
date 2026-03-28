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

import com.epam.digital.data.platform.management.reportexporter.client.QueryClient;
import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.Query;
import com.epam.digital.data.platform.management.reportexporter.model.Widget;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.epam.digital.data.platform.management.reportexporter.util.RedashResponseHandler.handleResponse;
import static java.lang.Math.max;
import static java.util.stream.Collectors.toSet;

@Component
public class QueryHelper {

  private final QueryClient queryClient;

  public QueryHelper(QueryClient queryClient) {
    this.queryClient = queryClient;
  }

  public Set<Query> getUtilQueries(Dashboard dashboard) {
    var queryIds = getUtilQueryIds(dashboard);
    var queries = getQueries(getQueryCount());

    return queries.stream()
        .filter(query -> queryIds.contains(query.getId()))
        .collect(toSet());
  }

  private Set<Integer> getUtilQueryIds(Dashboard dashboard) {
    return dashboard.getWidgets().stream()
        .filter(widget -> widget.getVisualization() != null)
        .flatMap(this::getParameterQueries)
        .collect(toSet());
  }

  private int getQueryCount() {
    return handleResponse(queryClient.getQueries(1)).getCount();
  }

  private List<Query> getQueries(int queryCount) {
    return handleResponse(queryClient.getQueries(max(1, queryCount))).getResults();
  }

  private Stream<Integer> getParameterQueries(Widget widget) {
    var options = widget.getVisualization().getQuery().getOptions();
    var queries = new HashSet<Integer>();

    var parameters = (List<Map<String, Object>>) options.get("parameters");

    for (var parameter : parameters) {
      if (parameter.get("type").equals("query")) {
        queries.add((Integer) parameter.get("queryId"));
      }
    }
    queries.add(widget.getVisualization().getQuery().getId());
    
    return queries.stream();
  }
}
