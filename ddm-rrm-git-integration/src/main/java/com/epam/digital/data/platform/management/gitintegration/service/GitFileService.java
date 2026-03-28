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
package com.epam.digital.data.platform.management.gitintegration.service;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.gitintegration.exception.GitCommandException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitFileService {

  private final VcsPropertiesConfig config;

  @NonNull
  public File writeFile(@NonNull String repositoryName, @NonNull String fileContent,
      @NonNull String filePath) {
    var repositoryDirectory = FilenameUtils.normalize(config.repositoryDirectory());
    var normalizedRepositoryName = FilenameUtils.normalize(repositoryName);
    var normalizedFilePath = FilenameUtils.normalize(filePath);

    var path = Path.of(repositoryDirectory, normalizedRepositoryName, normalizedFilePath);

    ensureDirectoryExists(path);

    try {
      Files.writeString(path, fileContent);
    } catch (IOException e) {
      throw new GitCommandException(
          String.format("Exception occurred during writing content to file %s: %s", filePath,
              e.getMessage()), e);
    }
    return path.toFile();
  }

  private static void ensureDirectoryExists(Path filePath) {
    var pathToDirectory = filePath.getParent();
    if (!Files.exists(pathToDirectory)) {
      try {
        Files.createDirectories(pathToDirectory);
      } catch (IOException e) {
        throw new GitCommandException(
            String.format("Exception occurred during creating directory %s: %s", pathToDirectory,
                e.getMessage()), e);
      }
    }
  }
}
