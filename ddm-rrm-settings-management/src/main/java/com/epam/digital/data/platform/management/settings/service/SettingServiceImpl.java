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
package com.epam.digital.data.platform.management.settings.service;

import com.epam.digital.data.platform.management.core.context.VersionContextComponentManager;
import com.epam.digital.data.platform.management.core.service.CacheService;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.filemanagement.service.VersionedFileRepository;
import com.epam.digital.data.platform.management.gitintegration.exception.GitFileNotFoundException;
import com.epam.digital.data.platform.management.settings.exception.SettingsParsingException;
import com.epam.digital.data.platform.management.settings.model.CamundaGlobalSystemVarsFileRepresentationDto;
import com.epam.digital.data.platform.management.settings.model.SettingsFileRepresentationDto;
import com.epam.digital.data.platform.management.settings.model.SettingsFileInfoDto;
import com.epam.digital.data.platform.management.settings.model.SettingsInfoDto;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettingServiceImpl implements SettingService {

  private static final String GLOBAL_SETTINGS_PATH = "global-vars/camunda-global-system-vars.yml";
  private static final String VERSION_SETTINGS_PATH = "settings/settings.yml";
  private static final String CAMUNDA_GLOBAL_SYSTEM_VARS_YML = "camunda-global-system-vars.yml";
  private static final String SETTINGS_YML = "settings.yml";
  private final VersionContextComponentManager versionContextComponentManager;
  private final CacheService cacheService;

  @Override
  public SettingsInfoDto getSettings(String versionCandidateId) {
    log.debug("Trying to get repo");
    var repo = versionContextComponentManager.getComponent(versionCandidateId, VersionedFileRepository.class);
    log.debug("Finished getting repo for {} version", versionCandidateId);
    String camundaGlobalVarsContent = repo.readFile(GLOBAL_SETTINGS_PATH);
    String settingsContent = repo.readFile(VERSION_SETTINGS_PATH);
    log.debug("Completed settings files reading");
    return parseSettingsFiles(camundaGlobalVarsContent, settingsContent);
  }

  @Override
  public void updateSettings(String versionCandidateId, SettingsInfoDto settings) {
    YAMLMapper mapper = new YAMLMapper(new YAMLFactory()).disable(
        YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
    log.debug("YAMLMapper was initialized. Trying to get repo");
    var repo = versionContextComponentManager.getComponent(versionCandidateId, VersionedFileRepository.class);
    log.debug("Finished getting repo for {} version", versionCandidateId);
    CamundaGlobalSystemVarsFileRepresentationDto camundaDto = new CamundaGlobalSystemVarsFileRepresentationDto(
        settings.getTheme(),
        settings.getSupportEmail());
    SettingsFileRepresentationDto settingDto = new SettingsFileRepresentationDto(
        settings.getTitleFull(),
        settings.getTitle()/*, settings.getBlacklistedDomains()*/); // TODO uncomment after validator-cli update
    writeSettingsContent(repo, mapper, settingDto);
    writeGlobalVarsContent(repo, mapper, camundaDto);
  }

  @Override
  public void rollbackSettingsFile(String settingsFileName, String versionCandidateId) {
    var repo = versionContextComponentManager.getComponent(versionCandidateId, VersionedFileRepository.class);
    switch (settingsFileName) {
      case CAMUNDA_GLOBAL_SYSTEM_VARS_YML:
        repo.rollbackFile(GLOBAL_SETTINGS_PATH);
        break;
      case SETTINGS_YML:
        repo.rollbackFile(VERSION_SETTINGS_PATH);
        break;
      default:
        throw new GitFileNotFoundException(String.format("Rollback failed, file %s doesn't exist", settingsFileName), settingsFileName);
      }
  }

  @Override
  public List<SettingsFileInfoDto> getChangedSettingsByVersion(String versionCandidateId) {
    return getSettingsFilesByVersion(versionCandidateId, FileStatus.UNCHANGED);
  }

  private List<SettingsFileInfoDto> getSettingsFilesByVersion(String versionCandidateId,
      FileStatus skippedStatus) {
    VersionedFileRepository repo = versionContextComponentManager.getComponent(versionCandidateId,
        VersionedFileRepository.class);
    List<String> conflicts = cacheService.getConflictsCache(versionCandidateId);
    List<SettingsFileInfoDto> settingsFileInfoDtos = new ArrayList<>();

    addSettingsFileDto(repo, conflicts, GLOBAL_SETTINGS_PATH, CAMUNDA_GLOBAL_SYSTEM_VARS_YML,
        skippedStatus, settingsFileInfoDtos);
    addSettingsFileDto(repo, conflicts, VERSION_SETTINGS_PATH, SETTINGS_YML, skippedStatus,
        settingsFileInfoDtos);

    return settingsFileInfoDtos;
  }

  private void addSettingsFileDto(VersionedFileRepository repo, List<String> conflicts, String path,
      String name, FileStatus skippedStatus, List<SettingsFileInfoDto> settingsFileInfoDtos) {
    var settingsFiles = repo.getFileList(path);
    if (!settingsFiles.isEmpty() && !skippedStatus.equals(settingsFiles.get(0).getStatus())) {
      var dto = SettingsFileInfoDto.builder()
          .path(path)
          .name(name)
          .status(settingsFiles.get(0).getStatus())
          .conflicted(conflicts.contains(path))
          .build();
      settingsFileInfoDtos.add(dto);
    }
  }

  private static SettingsInfoDto parseSettingsFiles(String camundaGlobalVarsContent,
      String settingsContent) {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .enable(JsonGenerator.Feature.IGNORE_UNKNOWN);
    log.debug("ObjectMapper was initialized");
    try {
      log.debug("Parsing settings files");
      SettingsFileRepresentationDto settingDto = mapper.readValue(settingsContent,
          SettingsFileRepresentationDto.class);
      log.debug("Parsed settings file");
      CamundaGlobalSystemVarsFileRepresentationDto camundaDto = mapper.readValue(
          camundaGlobalVarsContent, CamundaGlobalSystemVarsFileRepresentationDto.class);
      log.debug("Parsed global var file");
      return SettingsInfoDto.builder()
//          .blacklistedDomains(settingDto.getBlacklistedDomains()) TODO uncomment after validator-cli update
          .titleFull(settingDto.getTitleFull())
          .supportEmail(camundaDto.getSupportEmail())
          .theme(camundaDto.getTheme())
          .title(settingDto.getTitle())
          .build();
    } catch (JsonProcessingException e) {
      throw new SettingsParsingException("Could not process settings files", e);
    }
  }

  private void writeSettingsContent(VersionedFileRepository repo, ObjectMapper mapper,
      SettingsFileRepresentationDto settings) {
    try {
      log.debug("Writing settings to file");
      repo.writeFile(VERSION_SETTINGS_PATH, mapper.writeValueAsString(settings));
      log.debug("Finished writing settings");
    } catch (JsonProcessingException e) {
      throw new SettingsParsingException("Could not process settings file", e);
    }
  }

  private void writeGlobalVarsContent(VersionedFileRepository repo, ObjectMapper mapper,
      CamundaGlobalSystemVarsFileRepresentationDto globalVars) {
    try {
      log.debug("Writing global vars to file");
      repo.writeFile(GLOBAL_SETTINGS_PATH, mapper.writeValueAsString(globalVars));
      log.debug("Finished writing global vars");
    } catch (JsonProcessingException e) {
      throw new SettingsParsingException("Could not process global vars file", e);
    }
  }
}
