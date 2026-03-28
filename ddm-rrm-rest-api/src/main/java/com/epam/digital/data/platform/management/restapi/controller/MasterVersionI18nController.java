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

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
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
@Tag(description = "Registry regulations Master version I18n management Rest API", name = "master-version-i18n-api")
@RestController
@RequestMapping("/versions/master/i18n")
@RequiredArgsConstructor
public class MasterVersionI18nController {

  private static final String FINISHED_GETTING_I_18_N_BUNDLE_FROM_MASTER = "Finished getting {} i18n bundle from master";
  private final I18nService i18nService;
  private final VcsPropertiesConfig vcsPropertiesConfig;
  private final ControllerMapper mapper;

  @Operation(
      summary = "Get a list of i18n bundles with brief details for the master version",
      description = """
          ### Endpoint purpose:
          This endpoint is used for retrieving a list of JSON representations of __i18n bundles__ directly from the __master__ version, containing only brief information about each _i18n bundle_. If you need to retrieve full details of a single _i18n bundle_ based on its __name__, you can use the [GET](#master-version-i18n-api/getI18n) endpoint.""",
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
              content = @Content(
                  mediaType = MediaType.APPLICATION_JSON_VALUE,
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
  public ResponseEntity<List<I18nDetailsShort>> getI18nListFromMaster() {
    var masterVersionId = vcsPropertiesConfig.headBranch();
    log.info("Started getting i18n bundles from master");
    var i18nList = i18nService.getI18nListByVersion(masterVersionId);
    log.info("Found {} i18n bundles in master", i18nList.size());
    return ResponseEntity.ok().body(mapper.toI18nDetailsShortList(i18nList));
  }

  @Operation(
      summary = "Create new i18n bundle within master",
      description = """
          ### Endpoint purpose:
          This endpoint is used for creating a JSON representation of a __i18n bundle__ directly in the __master__ version. It is intended for situations that require the creation of a new _i18n bundle_. This operation creates a single _i18n bundle_ and should be used when multiple resources do not need to be created or modified simultaneously. If you need to create or modify several resources at once, it is still recommended to use a _version-candidate_.
          ### I18n bundle validation:
          Before saving the new _i18n bundle_ to the storage, the server validates the _i18n bundle_. The _i18n bundle_ must be a valid __json__ document
          ### Missing i18n bundle handling:
          If the specified _i18n bundle_ does not already exist, the server will create a new _i18n bundle_ with the provided data. If the _i18n bundle_ does exists, the server will return a _409 Conflict_ error indicating that the _i18n bundle_ already exists.""",
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
      })
  @PostMapping("/{name}")
  public ResponseEntity<String> i18nCreate(
      @RequestBody String i18n,
      @PathVariable
      @Parameter(description = "Name of the new i18n bundle to be created", required = true) String name) {
    var masterVersionId = vcsPropertiesConfig.headBranch();

    log.info("Started creating {} i18n bundle for master", name);
    i18nService.createI18n(name, i18n, masterVersionId);
    log.info("I18n bundle {} was created for master. Retrieving this i18n bundle", name);
    var response = i18nService.getI18nContent(name, masterVersionId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_MASTER, name);
    return ResponseEntity.created(URI.create(String.format("/versions/master/i18n/%s", name)))
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }

  @Operation(
      summary = "Get specific i18n bundle full details",
      description = """
          ### Endpoint purpose:
          This endpoint is used for retrieving a JSON representation of a __i18n bundle__ directly from the __master__ version. This operation retrieves a single _i18n bundle_ based on the specified __name__. If you need to retrieve list of _i18n bundles_, you can use the [GET](#master-version-i18n-api/getI18nListFromMaster) endpoint.""",
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
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Internal server error",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      })
  @GetMapping("/{name}")
  public ResponseEntity<Object> getI18n(
      @PathVariable
      @Parameter(description = "I18n bundle name", required = true) String name) {
    var masterVersionId = vcsPropertiesConfig.headBranch();
    log.info("Getting {} i18n bundle from master", name);
    var response = i18nService.getI18nContent(name, masterVersionId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_MASTER, name);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }

  @Operation(
      summary = "Delete existing i18n bundle within master",
      description = """
          ### Endpoint purpose:
          This endpoint is used for deleting a JSON representation of a __i18n bundle__ directly from the __master__ version.
          ### Conflict resolving:
          In this endpoint, [Conditional requests](https://datatracker.ietf.org/doc/html/rfc9110#name-conditional-requests) are supported. You can use an __ETag__ header value, which can be previously obtained in a [GET](#master-version-i18n-api/getI18n) request, as a value for the __If-Match__ header. This ensures that you're deleting the latest version of the _i18n bundle_. However, if your __If-Match__ value differs from the server's value, you will receive _409 Conflict_ instead of _412 Precondition Failed_. For the _registry-regulation-management_ service, this situation is considered a conflict. If the __If-Match__ header is not present, conflict checking will not be performed.
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
      })
  @DeleteMapping("/{name}")
  public ResponseEntity<String> deleteI18n(
      @PathVariable @Parameter(description = "Name of the i18n bundle to be deleted", required = true) String name,
      @RequestHeader HttpHeaders headers) {
    var masterVersionId = vcsPropertiesConfig.headBranch();
    var eTag = headers.getFirst("If-Match");
    log.info("Started deleting {} i18n bundle for master", name);
    i18nService.deleteI18n(name, masterVersionId, eTag);
    log.info("I18n bundle {} was deleted from master", name);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update existing i18n bundle within master version.",
      description = """
          ### Endpoint purpose:
          This endpoint is used for updating a json representation of a __i18n bundle__ directly in __master__ version. Just as if _version-candidate_ was created, the _i18n bundle_ was updated in that _version-candidate_ and then the _version-candidate_ was submitted. It can be used if there is needed to update __a single i18n bundle__. If you need to make some changes in several entities at one time, it's still preferred to make this changes through a _version-candidate_.
          ### Conflict resolving:
          In this endpoint [Conditional requests](https://datatracker.ietf.org/doc/html/rfc9110#name-conditional-requests) are supported. You can use an __ETag__ header value, that can be previously obtained in [GET](#master-version-i18n-api/getI18n) request, as a value for __If-Match__ header so you can be sure that you're updating the last version of an _i18n bundle_. But if your __If-Match__ value is differs from the servers you will receive a _409 Conflict_ instead of _412 Precondition Failed_. For _registry-regulation-management_ service this situation's considered as a conflict. If __If-Match__ is not present then conflict checking won't be performed.
          ### I18n bundle validation:
          Before saving the content to the storage, the __validation__ of a _i18n bundle_ is executed. The _i18n bundle_ must be a __json__ document
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
              responseCode = "400",
              description = "Request body is not a valid json",
              content = @Content
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Unauthorized",
              content = @Content
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Forbidden",
              content = @Content
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
              description = "Internal server error.",
              content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = DetailedErrorResponse.class))
          )
      })
  @PutMapping(value = "/{name}")
  public ResponseEntity<String> updateI18n(
      @RequestBody String i18n,
      @PathVariable
      @Parameter(description = "Name of the i18n bundle to be updated", required = true) String name,
      @RequestHeader HttpHeaders headers) {
    var masterVersionId = vcsPropertiesConfig.headBranch();
    var eTag = headers.getFirst("If-Match");

    log.info("Started updating {} i18n bundle for master", name);
    i18nService.updateI18n(i18n, name, masterVersionId, eTag);
    log.info("Finished updating {} i18n bundle for master. Retrieving this i18n bundle", name);
    var response = i18nService.getI18nContent(name, masterVersionId);
    log.info(FINISHED_GETTING_I_18_N_BUNDLE_FROM_MASTER, name);
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .eTag(ETagUtils.getETagFromContent(response))
        .body(response);
  }
}
