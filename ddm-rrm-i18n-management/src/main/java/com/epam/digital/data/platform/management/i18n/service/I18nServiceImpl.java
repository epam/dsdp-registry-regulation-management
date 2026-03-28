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

package com.epam.digital.data.platform.management.i18n.service;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.core.context.VersionContextComponentManager;
import com.epam.digital.data.platform.management.core.service.CacheService;
import com.epam.digital.data.platform.management.core.utils.ETagUtils;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.filemanagement.model.VersionedFileInfoDto;
import com.epam.digital.data.platform.management.filemanagement.service.VersionedFileRepository;
import com.epam.digital.data.platform.management.gitintegration.exception.FileAlreadyExistsException;
import com.epam.digital.data.platform.management.i18n.I18nMapper;
import com.epam.digital.data.platform.management.i18n.exception.I18nAlreadyExistsException;
import com.epam.digital.data.platform.management.i18n.exception.I18nNotFoundException;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class I18nServiceImpl implements I18nService {

  private static final String DIRECTORY_PATH = "i18n";
  private static final String JSON_FILE_EXTENSION = "json";

  private final VersionContextComponentManager versionContextComponentManager;
  private final CacheService cacheService;
  private final VcsPropertiesConfig vcsPropertiesConfig;

  private final I18nMapper i18nMapper;

  @Override
  public List<I18nInfoDto> getI18nListByVersion(String versionName) {
    return getI18nListByVersion(versionName, FileStatus.DELETED);
  }

  @Override
  public List<I18nInfoDto> getChangedI18nListByVersion(String versionName) {
    return getI18nListByVersion(versionName, FileStatus.UNCHANGED);
  }

  @Override
  public void createI18n(String i18nName, String content, String versionName)
      throws I18nAlreadyExistsException {
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    var i18nPath = getI18nPath(i18nName);
    if (repo.isFileExists(i18nPath)) {
      throw newI18nAlreadyExists(i18nName);
    }

    try {
      repo.writeFile(i18nPath, content);
    } catch (FileAlreadyExistsException e) {
      log.trace("Failed to create i18n bundle '{}' because it already exists", i18nName, e);
      throw newI18nAlreadyExists(i18nName);
    }
  }

  @Override
  public String getI18nContent(String i18nName, String versionName) {
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    repo.updateRepository();
    var i18nContent = repo.readFile(getI18nPath(i18nName));
    if (Objects.isNull(i18nContent)) {
      throw newI18nNotFoundException(i18nName);
    }
    return i18nContent;
  }

  @Override
  public void updateI18n(String content, String i18nName, String versionName, String eTag) {
    var i18nPath = getI18nPath(i18nName);
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    if (repo.isFileExists(i18nPath)) {
      var oldContent = repo.readFile(i18nPath);
      // ignore if no changes
      if (content.equals(oldContent)) {
        return;
      }
    }
    repo.writeFile(i18nPath, content, eTag);
  }

  @Override
  public void deleteI18n(String i18nName, String versionName, String eTag) {
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    var i18nPath = getI18nPath(i18nName);
    if (!repo.isFileExists(i18nPath)) {
      throw newI18nNotFoundException(i18nName);
    }
    repo.deleteFile(i18nPath, eTag);
  }

  @Override
  public void rollbackI18n(String i18nName, String versionName) {
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    var masterRepo = versionContextComponentManager.getComponent(
        vcsPropertiesConfig.headBranch(), VersionedFileRepository.class);
    var i18nPath = getI18nPath(i18nName);
    if (!repo.isFileExists(i18nPath) && !masterRepo.isFileExists(i18nPath)) {
      throw newI18nNotFoundException(i18nName);
    }
    repo.rollbackFile(i18nPath);
  }

  private String getI18nPath(String i18nName) {
    return String.format(
        "%s/%s.%s", DIRECTORY_PATH, FilenameUtils.getName(i18nName), JSON_FILE_EXTENSION);
  }

  private List<I18nInfoDto> getI18nListByVersion(String versionName, FileStatus skippedStatus) {
    var repo = versionContextComponentManager.getComponent(versionName,
        VersionedFileRepository.class);
    var fileList = repo.getFileList(DIRECTORY_PATH);
    var i18ns = new ArrayList<I18nInfoDto>();
    var conflicts = cacheService.getConflictsCache(versionName);

    for (VersionedFileInfoDto versionedFileInfoDto : fileList) {
      if (versionedFileInfoDto.getStatus().equals(skippedStatus)) {
        continue;
      }

      String content = null;
      if (!FileStatus.DELETED.equals(versionedFileInfoDto.getStatus())) {
        content = repo.readFile(versionedFileInfoDto.getPath());
      }

      i18ns.add(i18nMapper.toI18n(
          versionedFileInfoDto, ETagUtils.getETagFromContent(content),
          conflicts.contains(versionedFileInfoDto.getPath())));
    }
    return i18ns;
  }

  private I18nAlreadyExistsException newI18nAlreadyExists(String i18nName) {
    return new I18nAlreadyExistsException(
        String.format("I18n bundle '%s' already exists", i18nName), i18nName);
  }

  private static I18nNotFoundException newI18nNotFoundException(String i18nName) {
    return new I18nNotFoundException(String.format("I18n bundle %s not found", i18nName), i18nName);
  }
}