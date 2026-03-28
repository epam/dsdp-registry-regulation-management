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

import static org.mockito.ArgumentMatchers.anyString;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.core.context.VersionContextComponentManager;
import com.epam.digital.data.platform.management.core.service.CacheService;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.filemanagement.model.VersionedFileInfoDto;
import com.epam.digital.data.platform.management.filemanagement.service.VersionedFileRepository;
import com.epam.digital.data.platform.management.gitintegration.exception.FileAlreadyExistsException;
import com.epam.digital.data.platform.management.i18n.I18nMapper;
import com.epam.digital.data.platform.management.i18n.exception.I18nAlreadyExistsException;
import com.epam.digital.data.platform.management.i18n.exception.I18nNotFoundException;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import com.epam.digital.data.platform.management.i18n.util.TestUtils;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class I18nServiceImplTest {

  private static final String VERSION_ID = "version";
  private static final String MASTER_VERSION_ID = "master";

  private final String EN_CONTENT = TestUtils.getContent("en-sample.json");

  @InjectMocks
  private I18nServiceImpl i18nService;

  @Mock
  private VersionContextComponentManager versionContextComponentManager;
  @Mock
  private CacheService cacheService;
  @Spy
  private I18nMapper i18nMapper = Mappers.getMapper(I18nMapper.class);
  @Mock
  private VersionedFileRepository repository;
  @Mock
  private VcsPropertiesConfig vcsPropertiesConfig;

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    Mockito.doReturn(repository).when(versionContextComponentManager)
        .getComponent(VERSION_ID, VersionedFileRepository.class);
    Mockito.lenient().doReturn(MASTER_VERSION_ID).when(vcsPropertiesConfig).headBranch();
  }

  @Test
  void testGetI18nListByVersion() {
    var newI18n = VersionedFileInfoDto.builder().name("en").path("i18n/en.json")
        .status(FileStatus.NEW)
        .build();
    var deletedI18n = VersionedFileInfoDto.builder().status(FileStatus.DELETED).build();
    Mockito.when(repository.getFileList("i18n")).thenReturn(List.of(newI18n, deletedI18n));
    Mockito.when(repository.readFile("i18n/en.json")).thenReturn("someContent");
    Mockito.when(cacheService.getConflictsCache(VERSION_ID)).thenReturn(List.of("i18n/en.json"));

    var resultList = i18nService.getI18nListByVersion(VERSION_ID);

    var expectedI18nResponseDto = new I18nInfoDto("en", "i18n/en.json", FileStatus.NEW, true,
        String.format("\"%s\"", "someContent".hashCode()));
    Assertions.assertThat(resultList).hasSize(1).element(0).isEqualTo(expectedI18nResponseDto);
  }

  @Test
  void whenCreateI18nCalledAndFileAlreadyExistsThenI18nAlreadyExistsExceptionIsThrown() {
    Mockito.doReturn(true).when(repository).isFileExists("i18n/en.json");

    Assertions.assertThatThrownBy(() -> i18nService.createI18n("en", "someContent", VERSION_ID))
        .isInstanceOf(I18nAlreadyExistsException.class)
        .hasMessage("I18n bundle 'en' already exists");
  }

  @Test
  void whenCreateI18nCalledAndFileDoesNotExistThenFileIsCreated() {
    i18nService.createI18n("en", "someContent", VERSION_ID);
    Mockito.verify(repository).writeFile("i18n/en.json", "someContent");
  }

  @Test
  void whenCreateI18nCalledAndFileAlreadyExistsExceptionCaughtThenI18nAlreadyExistsExceptionIsThrown() {
    Mockito.doThrow(FileAlreadyExistsException.class).when(repository)
        .writeFile("i18n/en.json", "someContent");
    Assertions.assertThatThrownBy(() -> i18nService.createI18n("en", "someContent", VERSION_ID))
        .isInstanceOf(I18nAlreadyExistsException.class)
        .hasMessage("I18n bundle 'en' already exists");
    Mockito.verify(repository).writeFile("i18n/en.json", "someContent");
  }

  @Test
  @SneakyThrows
  void getChangedI18nListByVersionTest() {
    var newI18n = VersionedFileInfoDto.builder()
        .name("en")
        .path(null)
        .status(FileStatus.DELETED)
        .build();
    Mockito.when(repository.getFileList("i18n")).thenReturn(List.of(newI18n));

    var resultList = i18nService.getChangedI18nListByVersion(VERSION_ID);

    var expectedI18nResponseDto = new I18nInfoDto("en", null, FileStatus.DELETED, false, null);
    Assertions.assertThat(resultList).hasSize(1).element(0).isEqualTo(expectedI18nResponseDto);
  }

  @Test
  @SneakyThrows
  void getI18nContentTest() {
    Mockito.when(repository.readFile("i18n/en.json")).thenReturn(EN_CONTENT);

    var actualI18nContent = i18nService.getI18nContent("en", VERSION_ID);

    Assertions.assertThat(actualI18nContent).isEqualTo(EN_CONTENT);
    Mockito.verify(repository).updateRepository();
  }

  @Test
  @SneakyThrows
  void getI18nContentNullTest() {
    Mockito.when(repository.readFile("i18n/en.json")).thenReturn(null);

    Assertions.assertThatThrownBy(() -> i18nService.getI18nContent("en", VERSION_ID))
        .isInstanceOf(I18nNotFoundException.class)
        .hasMessage("I18n bundle en not found");
  }

  @Test
  @SneakyThrows
  void updateI18nTest() {
    Mockito.when(repository.isFileExists("i18n/en.json")).thenReturn(true);
    Mockito.when(repository.readFile("i18n/en.json")).thenReturn(EN_CONTENT);

    var newI18nContent = EN_CONTENT.replaceFirst(
        "\"localizedKey\": \"localizedMessage\"",
        "\"localizedKey\": \"updated localizedMessage\"");

    i18nService.updateI18n(newI18nContent, "en", VERSION_ID, null);
    Mockito.verify(repository).writeFile("i18n/en.json", newI18nContent, null);
  }

  @Test
  @SneakyThrows
  void updateI18nTest_noUpdates() {
    Mockito.when(repository.isFileExists("i18n/en.json")).thenReturn(true);
    Mockito.when(repository.readFile("i18n/en.json")).thenReturn(EN_CONTENT);

    i18nService.updateI18n(EN_CONTENT, "en", VERSION_ID, null);
    Mockito.verify(repository, Mockito.never()).writeFile(anyString(), anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void deleteI18nTest() {
    Mockito.doReturn(true).when(repository).isFileExists("i18n/en.json");

    Assertions.assertThatCode(() -> i18nService.deleteI18n("en", VERSION_ID, "eTag"))
        .doesNotThrowAnyException();

    Mockito.verify(repository).isFileExists("i18n/en.json");
    Mockito.verify(repository).deleteFile("i18n/en.json", "eTag");
  }

  @Test
  @SneakyThrows
  void deleteI18nTest_noI18nFound() {
    Assertions.assertThatThrownBy(() -> i18nService.deleteI18n("en", VERSION_ID, "eTag"))
        .isInstanceOf(I18nNotFoundException.class)
        .hasMessage("I18n bundle en not found");

    Mockito.verify(repository).isFileExists("i18n/en.json");
    Mockito.verify(repository, Mockito.never()).deleteFile("i18n/en.json", "eTag");
  }

  @Test
  @SneakyThrows
  void rollbackI18nTest() {
    Mockito.doReturn(true).when(repository).isFileExists("i18n/en.json");

    i18nService.rollbackI18n("en", VERSION_ID);

    Mockito.verify(repository).isFileExists("i18n/en.json");
    Mockito.verify(repository).rollbackFile("i18n/en.json");
  }

  @Test
  @SneakyThrows
  void rollbackI18nTest_noI18nFound() {
    var masterRepo = Mockito.mock(VersionedFileRepository.class);
    Mockito.doReturn(masterRepo).when(versionContextComponentManager)
        .getComponent(MASTER_VERSION_ID, VersionedFileRepository.class);

    Assertions.assertThatThrownBy(() -> i18nService.rollbackI18n("en", VERSION_ID))
        .isInstanceOf(I18nNotFoundException.class)
        .hasMessage("I18n bundle en not found");

    Mockito.verify(repository).isFileExists("i18n/en.json");
    Mockito.verify(repository, Mockito.never()).rollbackFile("i18n/en.json");
  }

}