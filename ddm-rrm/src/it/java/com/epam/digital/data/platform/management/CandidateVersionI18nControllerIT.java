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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("I18n in version candidates controller tests")
class CandidateVersionI18nControllerIT extends BaseIT {

  @Nested
  @DisplayName("GET /versions/candidates/{versionCandidateId}/i18n/{name}")
  class CandidateVersionI18nGetI18nByNameControllerIT {

    @Test
    @DisplayName("should return 200 with i18n bundle content")
    @SneakyThrows
    void getI18n() {
      // add file to "remote" repo
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/GET/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", expectedI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(expectedI18nContent),
          header().string(HttpHeaders.ETAG, String.format("\"%s\"", expectedI18nContent.hashCode()))
      );
    }

    @Test
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void getI18n_versionCandidateDoesNotExist() {
      // mock gerrit change info doesn't exist
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .accept(MediaType.TEXT_XML, MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
      );
    }

    @Test
    @DisplayName("should return 404 if i18n doesn't exist")
    @SneakyThrows
    void getI18n_i18nDoesNotExist() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .accept(MediaType.TEXT_XML, MediaType.APPLICATION_JSON)
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
  @DisplayName("GET /versions/candidates/{versionCandidateId}/i18n")
  class CandidateVersionI18nGetI18nListControllerIT {

    @Test
    @DisplayName("should return 200 with all found i18n bundles")
    @SneakyThrows
    void getI18nListByVersionId() {
      // add files to "remote" repo
      final var enContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/GET/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", enContent);
      final var ukContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/GET/uk.json");
      context.addFileToRemoteHeadRepo("/i18n/uk.json", ukContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n", versionCandidateId)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$[0].name", is("en")),
          jsonPath("$[0].eTag", is(String.format("\"%s\"", enContent.hashCode()))),
          jsonPath("$[1].name", is("uk")),
          jsonPath("$[1].eTag", is(String.format("\"%s\"", ukContent.hashCode())))
      );
    }

    @Test
    @DisplayName("should return 200 with empty array if there are no i18n bundles")
    @SneakyThrows
    void getI18nListByVersionId_noI18n() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n", versionCandidateId)
              .accept(MediaType.APPLICATION_JSON_VALUE)
      ).andExpectAll(
          status().isOk(),
          content().contentType("application/json"),
          jsonPath("$", hasSize(0))
      );
    }

    @Test
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void getI18nListByVersionId_versionCandidateDoesNotExist() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // perform query
      mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n", versionCandidateId)
              .accept(MediaType.APPLICATION_JSON_VALUE)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType("application/json"),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
      );
    }
  }

  @Nested
  @DisplayName("POST /versions/candidates/{versionCandidateId}/i18n/{name}")
  class CandidateVersionI18nCreateI18nByNameControllerIT {

    @Test
    @DisplayName("should return 201 and create i18n bundle if there's no such i18n bundle")
    @SneakyThrows
    void createI18n() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/POST/en.json");

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void createI18n_noVersionCandidate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/POST/en.json");

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
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

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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

    @Test
    @DisplayName("should get i18n added to remote after creating i18n")
    @SneakyThrows
    void shouldUpdateLocalRepoWhenCreateI18n() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to create
      final var i18nToCreate = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/POST/en.json");

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(i18nToCreate)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isCreated(),
          content().contentType(MediaType.APPLICATION_JSON),
          content().json(i18nToCreate)
      );

      Assertions.assertThat(context.getFileFromRemoteVersionCandidateRepo("/i18n/en.json"))
          .isNotNull();
    }
  }

  @Nested
  @DisplayName("PUT /versions/candidates/{versionCandidateId}/i18n/{name}")
  class CandidateVersionI18nUpdateI18nByNameControllerIT {

    @Test
    @DisplayName("should return 200 and update i18n bundle if there's already exists such i18n bundle and If-Match is not defined")
    @SneakyThrows
    void updateI18n_noETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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
    @DisplayName("should return 200 and update i18n bundle if there's already exists such i18n bundle and If-Match contains valid ETag")
    @SneakyThrows
    void updateI18n_validETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      //perform get
      MockHttpServletResponse response = mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")).andReturn().getResponse();

      //get eTag value from response
      var eTag = response.getHeader("ETag");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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
    void updateI18n_noI18nToUpdate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to create
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
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
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void updateI18n_noVersionCandidate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId,
              "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
      );
    }

    @Test
    @DisplayName("should return 409 if wrong ETag")
    @SneakyThrows
    void updateI18n_invalidETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .header("If-Match", RandomString.make())
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isConflict()
      );

      // assert that actual content was not updated
      final var actualI18nContent = context.getFileFromRemoteVersionCandidateRepo(
          "/i18n/en.json");

      JSONAssert.assertNotEquals(expectedI18nContent, actualI18nContent, true);
      JSONAssert.assertEquals(headI18nContent, actualI18nContent, true);
    }

    @Test
    @DisplayName("should return 409 if modified concurrently")
    @SneakyThrows
    void updateI18n_modifiedConcurrently() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // define modified i18n content to update
      final var modifiedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate-modified.json");

      //perform get
      var response = mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")).andReturn().getResponse();

      //get eTag value from response
      var eTag = response.getHeader("ETag");

      //perform update with missing eTag
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(modifiedI18nContent)
              .accept(MediaType.APPLICATION_JSON));

      // perform query with outdated ETag
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .header("If-Match", eTag)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isConflict()
      );

      // assert that actual content was not updated after second request
      final var actualI18nContent = context.getFileFromRemoteVersionCandidateRepo(
          "/i18n/en.json");
      JSONAssert.assertEquals(modifiedI18nContent, actualI18nContent, JSONCompareMode.LENIENT);
    }
  }

  @Nested
  @DisplayName("DELETE /versions/candidates/{versionCandidateId}/i18n/{name}")
  class CandidateVersionI18nDeleteI18nByNameControllerIT {

    @Test
    @DisplayName("should return 204 and delete i18n if there's already exists such i18n")
    @SneakyThrows
    void deleteI18n_noETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(delete(
          "/versions/candidates/{versionCandidateId}/i18n/{name}",
          versionCandidateId, "en")
      ).andExpect(
          status().isNoContent()
      );

      // assert that file is deleted
      final var isFileExists = context.isFileExistsInRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(isFileExists).isFalse();
    }

    @Test
    @DisplayName("should return 204 and delete i18n if there's already exists such i18n")
    @SneakyThrows
    void deleteI18n_validETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      //perform get
      MockHttpServletResponse response = mockMvc.perform(
          get("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")).andReturn().getResponse();

      //get eTag value from response
      String eTag = response.getHeader("ETag");

      // perform query
      mockMvc.perform(delete(
          "/versions/candidates/{versionCandidateId}/i18n/{name}",
          versionCandidateId, "en")
          .header("If-Match", eTag)
      ).andExpect(
          status().isNoContent()
      );

      // assert that file is deleted
      final var isFileExists = context.isFileExistsInRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(isFileExists).isFalse();
    }

    @Test
    @DisplayName("should return 409 with invalid ETag")
    @SneakyThrows
    void deleteI18n_invalidETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(delete(
          "/versions/candidates/{versionCandidateId}/i18n/{name}",
          versionCandidateId, "en")
          .header("If-Match", RandomString.make())
      ).andExpect(
          status().isConflict()
      );

      // assert that file was not deleted
      final var isFileExists = context.isFileExistsInRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(isFileExists).isTrue();
    }

    @Test
    @DisplayName("should return 204 and delete i18n with asterisk ETag")
    @SneakyThrows
    void deleteI18n_asteriskETag() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/DELETE/en.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(delete(
          "/versions/candidates/{versionCandidateId}/i18n/{name}",
          versionCandidateId, "en")
          .header("If-Match", "*")
      ).andExpect(
          status().isNoContent()
      );

      // assert that file is deleted
      final var isFileExists = context.isFileExistsInRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(isFileExists).isFalse();
    }

    @Test
    @DisplayName("should return 404 if there's no such i18n")
    @SneakyThrows
    void deleteI18n_noI18nToDelete() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          delete(
              "/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
      ).andExpectAll(
          status().isNotFound(),
          jsonPath("$.details", is("I18n bundle en not found")),
          jsonPath("$.messageKey", is("I18n bundle {{bundleName}} not found.")),
          jsonPath("$.messageParameters", is(Map.of("bundleName", "en")))
      );
    }

    @Test
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void deleteI18n_noVersionCandidate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // perform query
      mockMvc.perform(
          delete(
              "/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId, "en")
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
      );
    }
  }

  @Nested
  @DisplayName("POST /versions/candidates/{versionCandidateId}/i18n/{name}/rollback")
  class CandidateVersionI18nRollbackI18nByNameControllerIT {

    @Test
    @DisplayName("should return 200 and roll back i18n bundle if there's exists such i18n bundle")
    @SneakyThrows
    void rollbackI18n() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/rollback/POST/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // add modified state to version candidate
      final var modifiedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/rollback/POST/en-version-candidate.json");
      context.addFileToVersionCandidateRemote("/i18n/en.json", modifiedI18nContent);

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}/rollback",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk()
      );

      var actualFileContent = context.getFileFromRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(actualFileContent).isEqualTo(headI18nContent);
    }

    @Test
    @DisplayName("should return 200 and roll back i18n bundle if it was deleted in version candidate")
    @SneakyThrows
    void rollbackI18n_deleted() {
      // add file to "remote" repo
      final var headI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/rollback/POST/en-head.json");
      context.addFileToRemoteHeadRepo("/i18n/en.json", headI18nContent);
      context.pullHeadRepo();

      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();
      context.deleteFileFromVersionCandidateRemote("i18n/en.json");

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}/rollback",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isOk()
      );

      var actualFileContent = context.getFileFromRemoteVersionCandidateRepo("/i18n/en.json");
      Assertions.assertThat(actualFileContent).isEqualTo(headI18nContent);
    }

    @Test
    @DisplayName("should return 404 if there's no such i18n")
    @SneakyThrows
    void updateI18n_noI18nToUpdate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.createVersionCandidate();

      // perform query
      mockMvc.perform(
          post("/versions/candidates/{versionCandidateId}/i18n/{name}/rollback",
              versionCandidateId, "en")
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          jsonPath("$.details", is("I18n bundle en not found")),
          jsonPath("$.messageKey", is("I18n bundle {{bundleName}} not found.")),
          jsonPath("$.messageParameters", is(Map.of("bundleName", "en")))
      );
    }

    @Test
    @DisplayName("should return 404 if version-candidate doesn't exist")
    @SneakyThrows
    void updateI18n_noVersionCandidate() {
      // mock gerrit change info for version candidate
      final var versionCandidateId = context.mockVersionCandidateDoesNotExist();

      // define expected i18n content to update
      final var expectedI18nContent = context.getResourceContent(
          "/versions/candidates/{versionCandidateId}/i18n/{name}/PUT/en-version-candidate.json");

      // perform query
      mockMvc.perform(
          put("/versions/candidates/{versionCandidateId}/i18n/{name}",
              versionCandidateId,
              "en")
              .contentType(MediaType.APPLICATION_JSON)
              .content(expectedI18nContent)
              .accept(MediaType.APPLICATION_JSON)
      ).andExpectAll(
          status().isNotFound(),
          content().contentType(MediaType.APPLICATION_JSON),
          jsonPath("$.code", is("CHANGE_NOT_FOUND")),
          jsonPath("$.details",
              is(String.format("Could not get change info for %s MR", versionCandidateId)))
      );
    }
  }
}
