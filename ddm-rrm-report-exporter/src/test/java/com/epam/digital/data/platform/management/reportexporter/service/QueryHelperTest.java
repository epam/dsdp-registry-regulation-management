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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.management.reportexporter.client.QueryClient;
import com.epam.digital.data.platform.management.reportexporter.model.Dashboard;
import com.epam.digital.data.platform.management.reportexporter.model.Page;
import com.epam.digital.data.platform.management.reportexporter.model.Query;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
public class QueryHelperTest {

  QueryHelper instance;

  ObjectMapper objectMapper = new ObjectMapper();

  Dashboard dashboard;

  @Mock
  QueryClient queryClient;

  @BeforeEach
  void setup() throws IOException {
    instance = new QueryHelper(queryClient);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    dashboard = objectMapper
        .readValue(ResourceUtils.getFile("classpath:dashboards/dashboard.json"), Dashboard.class);
  }

  @Test
  void shouldReturnNotEmptySetWhenFoundParameterQueries() {
    var response = mockPageResponse(HttpStatus.OK, generateQueries(1, 6));
    when(queryClient.getQueries(anyInt())).thenReturn(response);

    var result = instance.getUtilQueries(dashboard);

    verify(queryClient).getQueries(1);
    verify(queryClient).getQueries(5);

    assertThat(result.size()).isEqualTo(4);
    assertThat(result)
        .containsExactlyInAnyOrder(generateQueries(1, 5).toArray(new Query[0]));
  }

  private ResponseEntity<Page<Query>> mockPageResponse(HttpStatus status, List<Query> queries) {
    var page = new Page<Query>();
    page.setCount(5);
    page.setResults(queries);

    return new ResponseEntity<>(page, status);
  }

  private List<Query> generateQueries(int startIndex, int endIndex) {
    var queries = new ArrayList<Query>();

    IntStream.range(startIndex, endIndex).forEach(id -> {
      var query = new Query();
      query.setId(id);
      query.setName("Query #" + id);
      query.setQuery("Query-" + id);
      queries.add(query);
    });

    return queries;
  }
}
