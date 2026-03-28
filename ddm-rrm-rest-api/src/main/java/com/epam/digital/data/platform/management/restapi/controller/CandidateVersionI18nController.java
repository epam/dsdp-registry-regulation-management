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

import com.epam.digital.data.platform.management.core.utils.ETagUtils;
import com.epam.digital.data.platform.management.i18n.service.I18nService;
import com.epam.digital.data.platform.management.restapi.mapper.ControllerMapper;
import com.epam.digital.data.platform.management.restapi.model.DetailedErrorResponse;
import com.epam.digital.data.platform.management.restapi.model.I18nDetailsShort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(description = "Registry regulations version-candidate I18n management Rest API", name = "candidate-version-i18n-api")
@RestController
@RequestMapping("/versions/candidates/{versionCandidateId}/i18n")
@RequiredArgsConstructor
public class CandidateVersionI18nController {

  private static final String FINISHED_GETTING_I_18_N_BUNDLE_FROM_VERSION_CANDIDATE = "Finished getting {} i18n bundle from {} version candidate";
  private final I18nService i18nService;
  private final ControllerMapper mapper;

  @Operation(
      summary = "Acquire list of i18n bundles with brief details for specific version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for retrieving a list of JSON representations of __i18n bundles__ from the __version-candidate__, containing only brief information about each _i18n bundle_. If you need to retrieve full details of a single _i18n bundle_ based on its __name__, you can use the [GET](#candidate-version-i18n-api/getI18n) endpoint.""",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK. I18n bundles successfully retrieved.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  array = @ArraySchema(schema = @Schema(implementation = I18nDetailsShort.class)),
                  examples = {
                      @ExampleObject(value = """
                          [
                            {
                              "name": "en"
                            },
                            {
                              "name": "uk"
                            }
                          ]""")
                  })
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      })
  @GetMapping
  public ResponseEntity<List<I18nDetailsShort>> getI18nListByVersionId(
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId) {
    log.info("Started getting i18n list for {} version candidate", versionCandidateId);
    var i18nList = i18nService.getI18nListByVersion(versionCandidateId);
    log.info("Found {} i18n bundles from {} version candidate", i18nList.size(),
        versionCandidateId);
    return ResponseEntity.ok().body(mapper.toI18nDetailsShortList(i18nList));
  }

  @Operation(
      summary = "Create new i18n bundle within specific version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for creating a JSON representation of a __i18n bundle__ in the __version-candidate__.
          ### I18n bundle validation:
          Before saving the new _i18n bundle_ to the storage, the server validates the _i18n bundle_. The _i18n bundle_ must be a valid __json__ document
          ### Missing i18n bundle handling:
          If the specified _i18n bundle_ does not already exist, the server will create a new _i18n bundle_ with the provided data. Otherwise, the server will return a _409 Conflict_ error indicating that the _i18n bundle_ already exists.""",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class),
              examples = {
                  @ExampleObject(value = """
                      {
                        "English translate": "Some other language translate"
                      }""")
              }
          )
      ),
      responses = {
          @ApiResponse(
              responseCode = "201",
              description = "I18n bundle successfully created",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                      @ExampleObject(value = """
                          {
                            "English translate": "Some other language translate"
                          }""")
                  }
              ),
              headers = @Header(name = HttpHeaders.ETAG, description = "New ETag value for conflict verification")
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "409",
              description = "Conflict. It means that i18n bundle already has been created.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "422",
              description = "Unprocessable Entity. I18n bundle is not valid.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @PostMapping("/{name}")
  public ResponseEntity<String> i18nCreate(@RequestBody String i18n,
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId,
      @PathVariable @Parameter(description = "Name of the new i18n bundle to be created", required = true) String name) {
    log.info("Started creating {} i18n for {} version candidate", name, versionCandidateId);
    i18nService.createI18n(name, i18n, versionCandidateId);
    log.info("I18n bundle {} was created for {} version candidate. Retrieving this i18n bundle",
        name, versionCandidateId);
    var response = i18nService.getI18nContent(name, versionCandidateId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_VERSION_CANDIDATE, name, versionCandidateId);
    return ResponseEntity.created(URI.create(
            String.format("/versions/candidates/%s/i18n/%s", versionCandidateId, name)))
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }

  @Operation(
      summary = "Get full details of the specific I18n bundle within version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for retrieving a JSON representation of a __i18n bundle__ from the __version-candidate__. This operation retrieves a single _I18n bundle_ based on the specified __name__. If you need to retrieve list of _i18n bundles_, you can use the [GET](#candidate-version-i18n-api/getI18nListByVersionId) endpoint.""",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "I18n bundle successfully retrieved.",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                      @ExampleObject(value = """
                          {
                            "English translate": "Some other language translate"
                          }""")
                  }
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Not Found",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @GetMapping("/{name}")
  public ResponseEntity<String> getI18n(
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId,
      @PathVariable @Parameter(description = "I18n name", required = true) String name) {
    log.info("Started getting {} i18n bundle from {} version candidate", name, versionCandidateId);
    var response = i18nService.getI18nContent(name, versionCandidateId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_VERSION_CANDIDATE, name, versionCandidateId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }

  @Operation(
      summary = "Update existing i18n bundle within version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for updating a json representation of a __i18n bundle__ in __version-candidate__.
          ### Conflict resolving:
          In this endpoint [Conditional requests](https://datatracker.ietf.org/doc/html/rfc9110#name-conditional-requests) are supported. You can use an __ETag__ header value, that can be previously obtained in [GET](#candidate-version-i18n-api/getI18n) request, as a value for __If-Match__ header so you can be sure that you're updating the last version of a _i18n bundle_. But if your __If-Match__ value is differs from the servers you will receive a _409 Conflict_ instead of _412 Precondition Failed_. For _registry-regulation-management_ service this situation's considered as a conflict. If __If-Match__ is not present then conflict checking won't be performed.
          ### I18n bundle validation:
          Before saving the content to the storage, the __validation__ of a _i18n bundle_ is executed. The _i18n bundle_ must be a __json__ document.
          ### Missing i18n bundle handling:
          If the updated _i18n bundle_ is missing and the _If-Match_ header is not present (or equal to __"*"__) then the _i18n bundle_ will be __created__ instead.""",
      parameters = {
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              in = ParameterIn.HEADER,
              name = "If-Match",
              description = "ETag to verify whether user has latest data",
              schema = @Schema(type = "string")
          )
      },
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
          content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Map.class),
              examples = {
                  @ExampleObject(value = """
                      {
                        "English translate": "Some other language translate"
                      }""")})),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "I18n bundle successfully updated.",
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
                  examples = {
                      @ExampleObject(value = """
                          {
                            "English translate": "Some other language translate"
                          }""")
                  }
              ),
              headers = {
                  @Header(name = HttpHeaders.ETAG, description = "New ETag value for conflict verification")
              }
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "409",
              description = "Conflict. __If-Match__ input value doesn't equal to servers value. It means that i18n bundle already has been updated/deleted after user obtained __ETag__.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "422",
              description = "Unprocessable Entity. I18n bundle is not valid.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @PutMapping(value = "/{name}")
  public ResponseEntity<String> updateI18n(@RequestBody String i18n,
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId,
      @PathVariable @Parameter(description = "Name of the i18n bundle to be updated", required = true) String name,
      @RequestHeader HttpHeaders headers) {
    var eTag = headers.getFirst("If-Match");
    log.info("Started updating {} i18n bundle for {} version candidate", name, versionCandidateId);
    i18nService.updateI18n(i18n, name, versionCandidateId, eTag);
    log.info(
        "Finished updating {} i18n bundle for {} version candidate. Retrieving this i18n bundle",
        name,
        versionCandidateId);
    var response = i18nService.getI18nContent(name, versionCandidateId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_VERSION_CANDIDATE, name, versionCandidateId);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }

  @Operation(
      summary = "Delete existing i18n bundle within version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for deleting a JSON representation of a __i18n bundle__ from the __version-candidate__.
          ### Conflict resolving:
          In this endpoint, [Conditional requests](https://datatracker.ietf.org/doc/html/rfc9110#name-conditional-requests) are supported. You can use an __ETag__ header value, which can be previously obtained in a [GET](#candidate-version-i18n-api/getI18n) request, as a value for the __If-Match__ header. This ensures that you're deleting the latest version of the _i18n bundle_. However, if your __If-Match__ value differs from the server's value, you will receive _409 Conflict_ instead of _412 Precondition Failed_. For the _registry-regulation-management_ service, this situation is considered a conflict. If the __If-Match__ header is not present, conflict checking will not be performed.
          ### Missing i18n bundle handling:
          If the specified _i18n bundle_ is missing and the _If-Match_ header is not present (or equal to __"*"__), the server will return a 404 Not Found error indicating that the specified _i18n bundle_ does not exist.""",
      parameters = {
          @Parameter(
              in = ParameterIn.HEADER,
              name = "X-Access-Token",
              description = "Token used for endpoint security",
              required = true,
              schema = @Schema(type = "string")
          ),
          @Parameter(
              in = ParameterIn.HEADER,
              name = "If-Match",
              description = "ETag to verify whether user has latest data",
              schema = @Schema(type = "string")
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "204",
              description = "No Content. I18n bundle successfully deleted.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Not Found",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "409",
              description = "Conflict. __If-Match__ input value doesn't equal to servers value. It means that i18n bundle already has been updated/deleted after user obtained __ETag__.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @DeleteMapping("/{name}")
  public ResponseEntity<String> deleteI18n(
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId,
      @PathVariable @Parameter(description = "Name of the i18n bundle to be deleted", required = true) String name,
      @RequestHeader HttpHeaders headers) {
    log.info("Started deleting {} i18n bundle for {} version candidate", name, versionCandidateId);
    var eTag = headers.getFirst("If-Match");
    i18nService.deleteI18n(name, versionCandidateId, eTag);
    log.info("I18n bundle {} was deleted from {} version candidate", name, versionCandidateId);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Rollback existing i18n bundle within version-candidate",
      description = """
          ### Endpoint purpose:
          This endpoint is used for rolling back a __i18n bundle__ from the __version-candidate__. It is intended for situations where a __i18n bundle__ needs to be reverted to a prior version, such as to mitigate data corruption or to restore a previous state.""",
      parameters = @Parameter(
          in = ParameterIn.HEADER,
          name = "X-Access-Token",
          description = "Token used for endpoint security",
          required = true,
          schema = @Schema(type = "string")
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "OK. I18n bundle successfully rolled back.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Not Found",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      }
  )
  @PostMapping("/{name}/rollback")
  public ResponseEntity<String> rollbackI18n(
      @PathVariable @Parameter(description = "Version candidate identifier", required = true) String versionCandidateId,
      @PathVariable @Parameter(description = "Name of the i18n bundle to be rolled back", required = true) String name) {
    log.info("Started rollback {} i18n bundle in {} version candidate", name, versionCandidateId);
    i18nService.rollbackI18n(name, versionCandidateId);
    log.info("Finished rolling back i18n bundle {} in the {} version candidate", name,
        versionCandidateId);
    return ResponseEntity.ok().build();
  }
}
