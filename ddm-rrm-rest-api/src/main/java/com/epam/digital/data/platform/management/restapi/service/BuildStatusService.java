/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.management.restapi.service;

import com.epam.digital.data.platform.management.core.config.CicdPropertiesConfig;
import com.epam.digital.data.platform.management.restapi.mapper.ControllerMapper;
import com.epam.digital.data.platform.management.restapi.model.BuildType;
import com.epam.digital.data.platform.management.restapi.model.ResultValues;
import com.epam.digital.data.platform.management.restapi.model.Validation;
import com.epam.digital.data.platform.management.versionmanagement.model.VersionInfoDto;
import com.epam.digital.data.platform.management.versionmanagement.service.VersionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BuildStatusService {

  private static final String BUILD_PENDING_STATUS = "Build Started";
  private static final List<String> BUILD_SUCCESS_STATUS = List.of("Build Successful", "Build Succeeded");
  private static final List<String> BUILD_FAILED_STATUS = List.of("Build Failed", "Build Aborted", "Build Unstable");

  private static final Map<BuildType, String> JENKINS_BUILD_NAMES =
      Map.of(BuildType.MASTER, "Build", BuildType.CANDIDATE, "Code-review");
  private static final Map<BuildType, String> TEKTON_BUILD_NAMES =
      Map.of(BuildType.MASTER, "build", BuildType.CANDIDATE, "code-review");

  private final VersionManagementService versionManagementService;
  private final ControllerMapper mapper;
  private final CicdPropertiesConfig cicdPropertiesConfig;

  public boolean isSuccessCandidateVersionBuild(String versionId) {
    VersionInfoDto versionDetails = versionManagementService.getVersionDetails(versionId);
    Map<String, Integer> labels = versionDetails.getLabels();
    Validation validation = mapper.toValidations(labels).get(0);
    return Objects.equals(validation.getResult(), ResultValues.SUCCESS);
  }

  public boolean isSuccessMasterVersionBuild() {
    var masterInfo = versionManagementService.getMasterInfo();
    String status = Objects.nonNull(masterInfo) ? getStatusVersionBuild(masterInfo, BuildType.MASTER) : "";
    return Objects.equals(status, ResultValues.SUCCESS.name());
  }

  public String getStatusVersionBuild(VersionInfoDto versionInfoDto, BuildType buildType) {
    var lastRevision = getVersionLatestRevision(versionInfoDto);
    var messageInfo =
        versionInfoDto.getMessages().stream()
            .filter(message -> lastRevision.equals(message._revisionNumber))
            .filter(
                message -> {
                  var patterns = getPossibleCicdPipeStatusPatterns(buildType);
                  return patterns.stream().anyMatch(pattern -> message.message.contains(pattern));
                })
            .max(Comparator.comparing(m -> m.date))
            .map(message -> message.message);
    var status = ResultValues.PENDING.name();
    if (messageInfo.isPresent()) {
      String mes = messageInfo.get();
      if (mes.contains(BUILD_PENDING_STATUS)) {
        status = ResultValues.PENDING.name();
      } else if (BUILD_SUCCESS_STATUS.stream().anyMatch(mes::contains)) {
        status = ResultValues.SUCCESS.name();
      } else if (BUILD_FAILED_STATUS.stream().anyMatch(mes::contains)) {
        status = ResultValues.FAILED.name();
      }
    }
    return status;
  }

  private List<String> getPossibleCicdPipeStatusPatterns(BuildType buildType) {
    var jenkinsPattern = String.format("%s-%s", JENKINS_BUILD_NAMES.get(buildType), cicdPropertiesConfig.getPipeName());
    var tektonPattern = String.format("%s-%s", cicdPropertiesConfig.getPipeName(), TEKTON_BUILD_NAMES.get(buildType));
    return List.of(jenkinsPattern, tektonPattern);
  }

  private Integer getVersionLatestRevision(VersionInfoDto versionInfoDto) {
    return versionInfoDto.getMessages().stream()
        .map(message -> message._revisionNumber)
        .filter(Objects::nonNull)
        .max(Integer::compare)
        .orElse(0);
  }
}
