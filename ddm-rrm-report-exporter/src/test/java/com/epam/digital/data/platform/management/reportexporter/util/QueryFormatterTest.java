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

package com.epam.digital.data.platform.management.reportexporter.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.epam.digital.data.platform.management.reportexporter.model.Query;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class QueryFormatterTest {

  @Test
  void shouldReturnCorrectPageWithQueries() {
    var queries = stubQueries();

    var result = QueryFormatter.formatQueryList(queries);
    var resultQueries = result.getResults();

    assertThat(result.getPage()).isEqualTo(1);
    assertThat(result.getPageSize()).isEqualTo(2);
    assertThat(result.getCount()).isEqualTo(2);
    assertThat(resultQueries.size()).isEqualTo(2);
  }

  private Set<Query> stubQueries() {
    var first = new Query();
    first.setQuery("first");
    first.setName("first");

    var second = new Query();
    second.setQuery("second");
    second.setName("second");

    return Set.of(first, second);
  }
}
