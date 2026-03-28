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

package com.epam.digital.data.platform.management;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.digital.data.platform.management.core.utils.ETagUtils;
import java.util.Map;
import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("I18n in master version controller tests")
class MasterVersionI18nControllerIT extends BaseIT {

  @Nested
  @DisplayName("GET /versions/master/i18n/{name}")
  class MasterVersionI18nGetI18nByNameControllerIT {

    @Test
    @DisplayName("should return 200 with i18n content")
    @SneakyThrows
    void getI18n() {
      // add file to "remote" repo and pull head repo
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/GET/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", expectedI18nContent);
      context.pullHeadRepo();

      // perform query
      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 200 if i18n added to remote")
    @SneakyThrows
    void getI18n_i18nHasBeenPulledWhenReadFile() {
      // add file to "remote" repo and DO NOT pull the head repo
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/GET/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", expectedI18nContent);

      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 404 if i18n doesn't exist")
    @SneakyThrows
    void getI18n_i18nDoesNotExist() {
      mockMvc.perform(
          get("/versions/master/i18n/{i18nName}", "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("I18N_NOT_FOUND")),
          jsonPath("$.details", is("I18n bundle en not found")),
          jsonPath("$.messageKey", is("I18n bundle {{bundleName}} not found.")),
          jsonPath("$.messageParameters", is(Map.of("bundleName", "en")))
      );
    }
  }

  @Nested
  @DisplayName("GET /versions/master/i18n")
  class MasterVersionI18nGetI18nListControllerIT {

    @Test
    @DisplayName("should return 200 with all pulled i18ns")
    @SneakyThrows
    void getI18nsInMaster() {
      // add 2 files to "remote" repo pull head branch repo and add 1 more file to "remote"
      final var enI18nContent = context.getResourceContent(
          "/versions/master/i18n/GET/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", enI18nContent);
      final var ukI18nContent = context.getResourceContent(
          "/versions/master/i18n/GET/uk.json");
      context.addFileToRemoteHeadRepo("/i18n/uk.json", ukI18nContent);
      context.pullHeadRepo();
      context.addFileToRemoteHeadRepo("/i18n/uk1.json", ukI18nContent);

      // perform query and expect only 2 of the values that are pulled on head-branch repo
      mockMvc.perform(
          get("/versions/master/i18n")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$", hasSize(2)),
          jsonPath("$[0].name", is("en")),
          jsonPath("$[0].eTag", is(String.format("\"%s\"", enI18nContent.hashCode()))),
          jsonPath("$[1].name", is("uk")),
          jsonPath("$[1].eTag", is(String.format("\"%s\"", ukI18nContent.hashCode())))
      );
    }

    @Test
    @DisplayName("should return 200 with empty array if there are no i18n")
    @SneakyThrows
    void getI18nsInMaster_noI18n() {
      mockMvc.perform(
          get("/versions/master/i18n")
              .accept(MediaType.APPLICATION_JSON_VALUE)
      ).andExpectAll(
          status().isOk(),
          content().contentType("application/json"),
          jsonPath("$", hasSize(0))
      );
    }
  }

  @Nested
  @DisplayName("POST /versions/master/i18n/{name}")
  class MasterVersionI18nCreateI18nControllerIT {

    @Test
    @DisplayName("should return 201 and create i18n if there's no such i18n")
    @SneakyThrows
    void createI18n() {
      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/POST/en.json");

      // perform query
      mockMvc.perform(
          post("/versions/master/i18n/{name}",
              "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isCreated(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 409 if there's already exists such i18n bundle")
    @SneakyThrows
    void createI18n_i18nAlreadyExists() {
      // add file to "remote" repo
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/POST/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", expectedI18nContent);
      context.pullHeadRepo();

      // perform query
      mockMvc.perform(
          post("/versions/master/i18n/{name}", "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isConflict(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("I18N_ALREADY_EXISTS")),
          jsonPath("$.details", is("I18n bundle 'en' already exists")),
          jsonPath("$.messageKey", is("I18n bundle {{bundleName}} already exists.")),
          jsonPath("$.messageParameters", is(Map.of("bundleName", "en")))
      );
    }
  }

  @Nested
  @DisplayName("DELETE /versions/master/i18n/{name}")
  class MasterVersionI18nDeleteI18nByNameControllerIT {

    @Test
    @DisplayName("should return 204 and delete i18n if there's already exists such i18n")
    @SneakyThrows
    void deleteI18n_noETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // perform query
      mockMvc.perform(delete("/versions/master/i18n/{name}", "en")
      ).andExpect(
          status().isNoContent()
      );

      // assert that file is deleted
      mockMvc.perform(get("/versions/master/i18n/{name}", "en")
          .accept(MediaType.APPLICATION_JSON)
      ).andExpect(
          status().isNotFound()
      );
    }

    @Test
    @DisplayName("should return 204 and delete i18n if there's already exists such i18n")
    @SneakyThrows
    void deleteI18n_validETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      //perform get
      MockHttpServletResponse response = mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
      ).andReturn().getResponse();

      //get eTag value from response
      String eTag = response.getHeader("ETag");

      // perform query
      mockMvc.perform(delete("/versions/master/i18n/{name}", "en")
          .header("If-Match", eTag)
      ).andExpect(
          status().isNoContent()
      );

      // assert that file is deleted
      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          jsonPath("$.code", is("I18N_NOT_FOUND")),
          jsonPath("$.details", is("I18n bundle en not found")),
          jsonPath("$.messageKey", is("I18n bundle {{bundleName}} not found.")),
          jsonPath("$.messageParameters", is(Map.of("bundleName", "en")))
      );
    }

    @Test
    @DisplayName("should return 409 with invalid ETag")
    @SneakyThrows
    void deleteI18n_invalidETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // perform query
      mockMvc.perform(delete("/versions/master/i18n/{name}", "en")
          .header("If-Match", RandomString.make())
      ).andExpect(
          status().isConflict()
      );

      // assert that file was not deleted
      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpect(
          status().isOk()
      );
    }

    @Test
    @DisplayName("should return 404 if there's no such i18n")
    @SneakyThrows
    void deleteI18n_noI18nToDelete() {
      // perform query
      mockMvc.perform(delete("/versions/master/i18n/{name}", "en")
      ).andExpect(
          status().isNotFound()
      );
    }
  }

  @Nested
  @DisplayName("PUT /versions/master/i18n/{name}")
  class MasterVersionI18nUpdateI18nByNameControllerIT {

    @Test
    @DisplayName("should return 200 and update i18n if there's already exists such i18n with no Etag defined")
    @SneakyThrows
    void updateI18n_noETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(put("/versions/master/i18n/{name}", "en")
          .contentType(MediaType.APPLICATION_JSON)
          .content(expectedI18nContent)
          .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 200 and update i18n if there's already exists such i18n and valid ETag defined")
    @SneakyThrows
    void updateI18n_validETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      //perform get
      var response = mockMvc.perform(get("/versions/master/i18n/{name}", "en")).andReturn()
          .getResponse();

      //get eTag value from response
      var eTag = response.getHeader("ETag");

      // perform query
      mockMvc.perform(
          put("/versions/master/i18n/{name}",
              "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .header("If-Match", eTag)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 200 and update i18n with asterisk ETag")
    @SneakyThrows
    void updateI18n_asteriskETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/master/i18n/{name}", "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .header("If-Match", "*")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 200 and create i18n if there's no such i18n")
    @SneakyThrows
    void updateI18n_noI18nsToUpdate() {
      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(put("/versions/master/i18n/{name}", "en")
          .contentType(MediaType.APPLICATION_JSON)
          .content(expectedI18nContent)
          .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 409 if wrong ETag")
    @SneakyThrows
    void updateI18n_invalidETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(put("/versions/master/i18n/{name}", "en")
          .contentType(MediaType.APPLICATION_JSON)
          .content(expectedI18nContent)
          .header("If-Match", RandomString.make())
          .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isConflict()
      );

      // assert that actual content was not updated
      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(headI18nContent)
      );
    }

    @Test
    @DisplayName("should return 409 if modified concurrently")
    @SneakyThrows
    void updateI18n_modifiedConcurrently() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");

      // define modified i18n content to update
      final var modifiedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate-modified.json");

      //perform get
      var response = mockMvc.perform(get("/versions/master/i18n/{name}", "en")).andReturn()
          .getResponse();

      //get eTag value from response
      var eTag = response.getHeader("ETag");

      //perform update with missing eTag
      mockMvc.perform(
          put("/versions/master/i18n/{name}", "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(modifiedI18nContent)
              .accept(MediaType.APPLICATION_JSON));

      // perform query with outdated ETag
      mockMvc.perform(
          put("/versions/master/i18n/{name}", "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .header("If-Match", eTag)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isConflict()
      );

      // assert that actual content was not updated after second request
      mockMvc.perform(
          get("/versions/master/i18n/{name}", "en")
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(modifiedI18nContent)
      );
    }

    @Test
    @DisplayName("should return 409 if there's no such i18n and IF-Match header present")
    @SneakyThrows
    void updateI18n_noI18nsToUpdateWithETag() {
      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/master/i18n/{name}/PUT/en-version-candidate.json");
      context.pullHeadRepo();

      // perform query
      mockMvc.perform(
          put("/versions/master/i18n/{name}", "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
              .header("IF-Match", ETagUtils.getETagFromContent(expectedI18nContent))
      ).andExpectAll(
          status().isConflict()
      );
    }
  }
}
