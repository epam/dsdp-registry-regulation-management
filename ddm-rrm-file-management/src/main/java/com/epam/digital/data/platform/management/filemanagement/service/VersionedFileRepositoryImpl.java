/*
 * Copyright 2024 EPAM Systems.
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
package com.epam.digital.data.platform.management.filemanagement.service;

import com.epam.digital.data.platform.management.filemanagement.mapper.FileManagementMapper;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.filemanagement.model.VersionedFileInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.model.ChangeInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.model.FileInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.service.VcsService;
import com.epam.digital.data.platform.management.gitintegration.exception.RepositoryNotFoundException;
import com.epam.digital.data.platform.management.gitintegration.service.JGitService;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.NonNull;


public class VersionedFileRepositoryImpl extends AbstractVersionFileRepository {

  public VersionedFileRepositoryImpl(String versionId, JGitService gitService,
                                     VcsService vcsService, FileManagementMapper mapper) {
    super(versionId, gitService, vcsService, mapper);
  }

  @Override
  @NonNull
  public List<VersionedFileInfoDto> getFileList(@NonNull String path) {
    Map<String, VersionedFileInfoDto> filesInMaster = gitService.getFilesInPath(versionId, path)
        .stream()
        .filter(Predicate.not(DOT_GIT_KEEP::equals))
        .map(el -> {
          var filePath = FilenameUtils.normalize(Path.of(path, el).toString(), true);
          return mapper.toVersionedFileInfoDto(filePath);
        })
        .collect(Collectors.toMap(VersionedFileInfoDto::getName, Function.identity()));

    vcsService.getListOfChangesInMR(getChangeId()).forEach((key, value) -> {
      if (key.startsWith(path)) {
        VersionedFileInfoDto filesResponseDto = searchFileInMap(filesInMaster, key);
        if (filesResponseDto == null) {
          filesInMaster.put(FilenameUtils.getBaseName(key), VersionedFileInfoDto.builder()
              .name(FilenameUtils.getBaseName(key))
              .status(getStatus(value))
              .build());
        } else {
          filesResponseDto.setStatus(getStatus(value));
        }
      }
    });
    var forms = new ArrayList<>(filesInMaster.values());
    forms.sort(Comparator.comparing(VersionedFileInfoDto::getName));
    return forms;
  }

  private VersionedFileInfoDto searchFileInMap(Map<String, VersionedFileInfoDto> map,
      String fileKey) {
    return map.get(FilenameUtils.getBaseName(fileKey));
  }

  @Override
  public void writeFile(@NonNull String path, @NonNull String content) {
    updateRepository();
    gitService.amend(versionId, path, content, null);
  }

  @Override
  public void writeFile(@NonNull String path, @NonNull String content, String eTag) {
    updateRepository();
    gitService.amend(versionId, path, content, eTag);
  }

  @Override
  public boolean isFileExists(@NonNull String path) {
    Path normalized = Path.of(path).normalize();
    String baseName = FilenameUtils.getBaseName(normalized.getFileName().toString());
    String parent = normalized.getParent() != null ? normalized.getParent().toString() : "";
    return getFileList(parent).stream()
        .filter(fileResponse -> !FileStatus.DELETED.equals(fileResponse.getStatus()))
        .anyMatch(f -> baseName.equals(f.getName()));
  }

  @Override
  public void deleteFile(@NonNull String path, String eTag) {
    updateRepository();
    gitService.delete(versionId, path, eTag);
  }

  @Override
  public void updateRepository() {
    var changeId = getChangeId();
    if (changeId == null) {
      throw new RepositoryNotFoundException("Version " + versionId + " not found", versionId);
    } else {
      gitService.cloneRepoIfNotExist(versionId);
      var changeInfo = vcsService.getChangeInfo(changeId);
      gitService.fetch(versionId, changeInfo.getRefs());
    }
  }

  @Override
  public void rollbackFile(@NonNull String filePath) {
    updateRepository();
    gitService.rollbackFile(versionId, filePath);
  }

  private String getChangeId() {
    ChangeInfoDto changeInfo = vcsService.getMRByNumber(versionId);
    return changeInfo != null ? changeInfo.getChangeId() : null;
  }

  private FileStatus getStatus(FileInfoDto fileInfo) {
    String status = fileInfo.getStatus();
    if (Objects.isNull(status) || "R".equals(status)) {
      return FileStatus.CHANGED;
    }
    if ("A".equals(status) || "C".equals(status)) {
      return FileStatus.NEW;
    }
    if ("D".equals(status)) {
      return FileStatus.DELETED;
    }
    return null;
  }
}
