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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;

import com.epam.digital.data.platform.management.core.context.VersionContextComponentManager;
import com.epam.digital.data.platform.management.core.service.CacheService;
import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.filemanagement.model.VersionedFileInfoDto;
import com.epam.digital.data.platform.management.filemanagement.service.VersionedFileRepository;
import com.epam.digital.data.platform.management.gitintegration.exception.GitFileNotFoundException;
import com.epam.digital.data.platform.management.settings.exception.SettingsParsingException;
import com.epam.digital.data.platform.management.settings.model.SettingsFileInfoDto;
import com.epam.digital.data.platform.management.settings.model.SettingsInfoDto;
import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class SettingServiceTest {

  private static final String VERSION_ID = "version";
  private static final String GLOBAL_VARS_PATH = "global-vars/camunda-global-system-vars.yml";
  private static final String SETTINGS_PATH = "settings/settings.yml";
  private static final String CAMUNDA_GLOBAL_SYSTEM_VARS_YML = "camunda-global-system-vars.yml";
  private static final String SETTINGS_YML = "settings.yml";

  @Captor
  private ArgumentCaptor<String> captor;

  @Mock
  private VersionContextComponentManager versionContextComponentManager;
  @Mock
  private VersionedFileRepository repository;
  @Mock
  private CacheService cacheService;
  @InjectMocks
  private SettingServiceImpl settingServiceImpl;

  private static final String GLOBAL_SETTINGS_VALUE =
          """
                  supportEmail: "support@registry.gov.ua"
                  theme: "white-theme"
                  """;
  private static final String SETTINGS_VALUE = """
          settings:
            general:
              validation:
                email:
                  blacklist:
                    domains:
                    - "ya.ua"
                    - "ya.ru"
              titleFull: "<Назва реєстру>"
              title: "mdtuddm"
          """;

  private static final String SETTINGS_EMPTY_CONTENT = """
          settings:
            general:
              titleFull: null
              title: null
          """;
  private static final String GLOBAL_SETTINGS_EMPTY_VALUE = """
          theme: null
          supportEmail: null
          """;


  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    Mockito.when(versionContextComponentManager.getComponent(VERSION_ID, VersionedFileRepository.class))
        .thenReturn(repository);
  }

  @Test
  @SneakyThrows
  void setSettingsTest() {
    Mockito.when(repository.readFile(GLOBAL_VARS_PATH))
        .thenReturn(GLOBAL_SETTINGS_VALUE);
    Mockito.when(repository.readFile(SETTINGS_PATH)).thenReturn(SETTINGS_VALUE);
    SettingsInfoDto expected = SettingsInfoDto.builder()
        .supportEmail("support@registry.gov.ua")
        .title("mdtuddm")
        .titleFull("<Назва реєстру>")
//        .blacklistedDomains(List.of("ya.ua", "ya.ru")) TODO uncomment after validator-cli update
        .theme("white-theme")
        .build();
    settingServiceImpl.updateSettings(VERSION_ID, expected);
    SettingsInfoDto actual = settingServiceImpl.getSettings(VERSION_ID);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void setSettingsNoErrorTest() {
    var settings = SettingsInfoDto.builder().build();
    Assertions.assertThatCode(() -> settingServiceImpl.updateSettings(VERSION_ID, settings))
        .doesNotThrowAnyException();
    Mockito.verify(repository)
        .writeFile(eq("settings/settings.yml"), captor.capture());
    String settingsContent = captor.getValue();
    Mockito.verify(repository)
        .writeFile(eq("global-vars/camunda-global-system-vars.yml"), captor.capture());
    String globalVars = captor.getValue();
    Assertions.assertThat(settingsContent).isEqualTo(SETTINGS_EMPTY_CONTENT);
    Assertions.assertThat(globalVars).isEqualTo(GLOBAL_SETTINGS_EMPTY_VALUE);
    //check if there is no error, but not real value
  }

  @Test
  @SneakyThrows
  void getSettings() {
    Mockito.when(repository.readFile(GLOBAL_VARS_PATH))
        .thenReturn(GLOBAL_SETTINGS_VALUE);
    Mockito.when(repository.readFile(SETTINGS_PATH)).thenReturn(SETTINGS_VALUE);
    SettingsInfoDto expected = SettingsInfoDto.builder()
        .supportEmail("support@registry.gov.ua")
        .title("mdtuddm")
        .titleFull("<Назва реєстру>")
//        .blacklistedDomains(List.of("ya.ua", "ya.ru")) TODO uncomment after validator-cli update
        .theme("white-theme")
        .build();
    SettingsInfoDto actual = settingServiceImpl.getSettings(VERSION_ID);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @SneakyThrows
  void getSettingsInvalidContent() {
    Mockito.when(repository.readFile(GLOBAL_VARS_PATH))
        .thenReturn("Illegal settings");
    Mockito.when(repository.readFile(SETTINGS_PATH))
        .thenReturn("Illegal global vars");
    assertThatThrownBy(() -> settingServiceImpl.getSettings(VERSION_ID))
        .isInstanceOf(SettingsParsingException.class)
        .hasMessage("Could not process settings files");
  }

  @Test
  @SneakyThrows
  void getChangedSettingsFiles() {
    var globalSettingsFile = VersionedFileInfoDto.builder()
        .name(CAMUNDA_GLOBAL_SYSTEM_VARS_YML)
        .path(GLOBAL_VARS_PATH)
        .status(FileStatus.CHANGED)
        .build();
    var settings = VersionedFileInfoDto.builder()
        .name(SETTINGS_YML)
        .path(SETTINGS_PATH)
        .status(FileStatus.UNCHANGED)
        .build();
    Mockito.when(repository.getFileList(GLOBAL_VARS_PATH)).thenReturn(List.of(globalSettingsFile));
    Mockito.when(repository.getFileList(SETTINGS_PATH)).thenReturn(List.of(settings));

    var resultList = settingServiceImpl.getChangedSettingsByVersion(VERSION_ID);
    var expectedResult =
        List.of(SettingsFileInfoDto.builder()
            .name(CAMUNDA_GLOBAL_SYSTEM_VARS_YML)
            .path(GLOBAL_VARS_PATH)
            .status(FileStatus.CHANGED)
            .conflicted(false)
            .build()
        );
    assertThat(resultList).hasSize(1).isEqualTo(expectedResult);
  }

  @Test
  @SneakyThrows
  void rollbackSettingsFileTest() {
    settingServiceImpl.rollbackSettingsFile(CAMUNDA_GLOBAL_SYSTEM_VARS_YML, VERSION_ID);

    Mockito.verify(repository).rollbackFile(GLOBAL_VARS_PATH);
  }

  @Test
  @SneakyThrows
  void rollbackSettingsFileNotExists() {
    assertThatThrownBy(() -> settingServiceImpl.rollbackSettingsFile("SomeFile.yaml", VERSION_ID))
        .isInstanceOf(GitFileNotFoundException.class)
        .hasMessage("Rollback failed, file SomeFile.yaml doesn't exist");
  }
}