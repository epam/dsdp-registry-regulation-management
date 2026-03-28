/*
 * Copyright 2024 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.management.restapi.controller;

import static org.hamcrest.Matchers.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import com.epam.digital.data.platform.management.i18n.service.I18nService;
import com.epam.digital.data.platform.management.restapi.util.TestUtils;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ControllerTest(MasterVersionI18nController.class)
@DisplayName("I18n in master version controller tests")
class MasterVersionI18nControllerTest {

  static final String HEAD_BRANCH = "master";

  MockMvc mockMvc;

  @MockBean
  I18nService i18nService;
  @MockBean
  VcsPropertiesConfig vcsPropertiesConfig;

  @BeforeEach
  void setUp(WebApplicationContext webApplicationContext,
      RestDocumentationContextProvider restDocumentation) {
    Mockito.lenient().doReturn(HEAD_BRANCH).when(vcsPropertiesConfig).headBranch();
    this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        .apply(documentationConfiguration(restDocumentation))
        .build();
  }

  @Test
  @DisplayName("GET /versions/master/i18n should return 200 with list of all i18n bundles")
  @SneakyThrows
  void getI18nListFromMaster() {
    var expectedI18nResponse = new I18nInfoDto("en", "i18n/en.json", FileStatus.UNCHANGED, false, "someEtag");
    Mockito.doReturn(List.of(expectedI18nResponse))
        .when(i18nService).getI18nListByVersion(HEAD_BRANCH);

    mockMvc.perform(
        get("/versions/master/i18n")
    ).andExpectAll(
        status().isOk(),
        content().contentType(MediaType.APPLICATION_JSON),
        jsonPath("$.[0].name", is("en")),
        jsonPath("$.[0].eTag", is("someEtag"))
    ).andDo(document("versions/master/i18n/GET"));

    Mockito.verify(i18nService).getI18nListByVersion(HEAD_BRANCH);
  }

  @Test
  @DisplayName("GET /versions/master/i18n/{name} should return 200 with i18n bundle content")
  @SneakyThrows
  void getI18nFromMaster() {
    final var name = "en";
    final var expectedI18nContent = TestUtils.getContent("controller/en.json");
    Mockito.doReturn(expectedI18nContent)
        .when(i18nService).getI18nContent(name, HEAD_BRANCH);

    mockMvc.perform(
        get("/versions/master/i18n/{name}", name)
    ).andExpectAll(
        status().isOk(),
        content().contentType(MediaType.APPLICATION_JSON),
        content().json(expectedI18nContent)
    ).andDo(document("versions/master/i18n/{name}/GET"));

    Mockito.verify(i18nService).getI18nContent(name, HEAD_BRANCH);
  }

  @Test
  @DisplayName("POST /versions/master/i18n/{name} should return 201 with i18n bundle content")
  @SneakyThrows
  void i18nCreateTest() {
    final var name = "en";
    final var expectedI18nContent = TestUtils.getContent("controller/en.json");

    Mockito.doReturn(expectedI18nContent)
        .when(i18nService).getI18nContent(name, HEAD_BRANCH);

    mockMvc.perform(
        post("/versions/master/i18n/{name}", name)
            .contentType(MediaType.APPLICATION_JSON)
            .content(expectedI18nContent)
    ).andExpectAll(
        status().isCreated(),
        header().string(HttpHeaders.LOCATION, "/versions/master/i18n/en"),
        content().contentType(MediaType.APPLICATION_JSON),
        content().json(expectedI18nContent)
    ).andDo(document("versions/master/i18n/{name}/POST"));

    Mockito.verify(i18nService).createI18n(name, expectedI18nContent, HEAD_BRANCH);
  }

  @Test
  @DisplayName("PUT /versions/master/i18n/{name} should return 200 with i18n bundle content")
  @SneakyThrows
  void updateI18nTest() {
    final var name = "en";
    final var expectedI18nContent = TestUtils.getContent("controller/en.json");

    Mockito.doReturn(expectedI18nContent)
        .when(i18nService).getI18nContent(name, HEAD_BRANCH);

    mockMvc.perform(
        put("/versions/master/i18n/{name}", name)
            .contentType(MediaType.APPLICATION_JSON)
            .content(expectedI18nContent)
    ).andExpectAll(
        status().isOk(),
        content().json(expectedI18nContent)
    ).andDo(document("versions/master/i18n/{name}/PUT"));

    Mockito.verify(i18nService).updateI18n(expectedI18nContent, name, HEAD_BRANCH, null);
  }

  @Test
  @DisplayName("DELETE /versions/master/i18n/{name} should return 204")
  @SneakyThrows
  void deleteI18nTest() {
    Mockito.doNothing().when(i18nService).deleteI18n("en", HEAD_BRANCH, "eTag");

    mockMvc.perform(delete("/versions/master/i18n/{name}", "en").header("IF-Match", "eTag")
    ).andExpect(
        status().isNoContent()
    ).andDo(document("versions/master/i18n/{name}/DELETE"));

    Mockito.verify(i18nService).deleteI18n("en", HEAD_BRANCH, "eTag");
  }
}
