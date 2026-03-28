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

import com.epam.digital.data.platform.management.reportexporter.model.Page;
import com.epam.digital.data.platform.management.reportexporter.model.Query;

import java.util.ArrayList;
import java.util.Set;

public class QueryFormatter {

  private QueryFormatter() {}

  public static Page<Query> formatQueryList(Set<Query> queries) {
    var queryPage = new Page<Query>();

    queryPage.setPage(1);
    queryPage.setPageSize(queries.size());
    queryPage.setCount(queries.size());
    queryPage.setResults(new ArrayList<>(queries));

    return queryPage;
  }
}
