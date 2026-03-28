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

package com.epam.digital.data.platform.management.versionmanagement.service;

import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;
import com.epam.digital.data.platform.management.forms.model.FormInfoDto;
import com.epam.digital.data.platform.management.groups.model.GroupChangesDetails;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import com.epam.digital.data.platform.management.model.dto.BusinessProcessInfoDto;
import com.epam.digital.data.platform.management.model.dto.DataModelFileDto;
import com.epam.digital.data.platform.management.model.dto.DataModelFileStatus;
import com.epam.digital.data.platform.management.model.dto.DataModelFileType;
import com.epam.digital.data.platform.management.versionmanagement.model.DataModelChangesInfoDto;
import com.epam.digital.data.platform.management.versionmanagement.model.EntityChangesInfoDto;
import com.epam.digital.data.platform.management.versionmanagement.model.EntityChangesInfoDto.ChangedFileStatus;
import java.util.List;
import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("VersionManagementService#getVersionChanges(String)")
class VersionManagementServiceGetVersionChangesTest extends VersionManagementServiceBaseTest {

  @Test
  @DisplayName("should return an object with all found changes")
  @SneakyThrows
  void getVersionChangesTest() {
    final var changeId = RandomString.make();

    mockFormList(changeId);
    mockBpList(changeId);
    mockDataModelList(changeId);
    mockI18nList(changeId);
    var group = GroupChangesDetails.builder()
        .name("bp-grouping.yml")
        .status(FileStatus.NEW)
        .conflicted(true)
        .build();
    Mockito.when(groupService.getChangesByVersion(changeId)).thenReturn(group);

    final var actualVersionChanges = managementService.getVersionChanges(changeId);

    Assertions.assertThat(actualVersionChanges).isNotNull();
    Assertions.assertThat(actualVersionChanges.getChangedForms())
        .containsAll(expectedFormChanges());
    Assertions.assertThat(actualVersionChanges.getChangedI18nFiles())
        .containsAll(expectedI18nChanges());
    Assertions.assertThat(actualVersionChanges.getChangedBusinessProcesses())
        .containsAll(expectedBpChanges());
    Assertions.assertThat(actualVersionChanges.getChangedDataModelFiles())
        .containsAll(expectedDataModelChanges());
    final var changedGroups = actualVersionChanges.getChangedGroups();
    Assertions.assertThat(changedGroups.get(0).getStatus().name())
        .isEqualTo(group.getStatus().name());
    Assertions.assertThat(changedGroups.get(0).getName()).isEqualTo(group.getName());
    Assertions.assertThat(changedGroups.get(0).isConflicted()).isTrue();

    Mockito.verify(formService).getChangedFormsListByVersion(changeId);
    Mockito.verify(businessProcessService).getChangedProcessesByVersion(changeId);
    Mockito.verify(dataModelService).listDataModelFiles(changeId);
    Mockito.verify(groupService).getChangesByVersion(changeId);
    Mockito.verify(i18nService).getChangedI18nListByVersion(changeId);
  }

  private static List<DataModelChangesInfoDto> expectedDataModelChanges() {
    return List.of(
        DataModelChangesInfoDto.builder()
            .name("createTables")
            .fileType(DataModelChangesInfoDto.DataModelFileType.TABLES_FILE)
            .status(DataModelChangesInfoDto.DataModelFileStatus.CHANGED)
            .conflicted(true)
            .build()
    );
  }

  private static List<EntityChangesInfoDto> expectedBpChanges() {
    return List.of(
        EntityChangesInfoDto.builder()
            .name("business-process")
            .title("Really test name")
            .status(ChangedFileStatus.NEW)
            .conflicted(false)
            .build(),
        EntityChangesInfoDto.builder()
            .name("changed-business-process")
            .title("Changed test name")
            .status(ChangedFileStatus.CHANGED)
            .conflicted(true)
            .build(),
        EntityChangesInfoDto.builder()
            .name("deleted-business-process")
            .title("Deleted test name")
            .status(ChangedFileStatus.DELETED)
            .conflicted(false)
            .build()
    );
  }

  private static List<EntityChangesInfoDto> expectedFormChanges() {
    return List.of(
        EntityChangesInfoDto.builder()
            .name("new_form")
            .status(ChangedFileStatus.NEW)
            .title("New Form")
            .conflicted(false)
            .build(),
        EntityChangesInfoDto.builder()
            .name("changed_form")
            .status(ChangedFileStatus.CHANGED)
            .title("Changed Form")
            .conflicted(true)
            .build(),
        EntityChangesInfoDto.builder()
            .name("deleted_form")
            .status(ChangedFileStatus.DELETED)
            .title("Deleted Form")
            .conflicted(false)
            .build()
    );
  }

  private static List<EntityChangesInfoDto> expectedI18nChanges() {
    return List.of(
        EntityChangesInfoDto.builder()
            .name("en")
            .status(ChangedFileStatus.NEW)
            .conflicted(false)
            .build(),
        EntityChangesInfoDto.builder()
            .name("fr")
            .status(ChangedFileStatus.CHANGED)
            .conflicted(true)
            .build(),
        EntityChangesInfoDto.builder()
            .name("pl")
            .status(ChangedFileStatus.DELETED)
            .conflicted(false)
            .build()
    );
  }

  private void mockDataModelList(String changeId) {
    var dataModelChanges = List.of(
        DataModelFileDto.builder()
            .fileName("newDataModelFile")
            .type(null)
            .status(DataModelFileStatus.NEW)
            .conflicted(false)
            .build(),
        DataModelFileDto.builder()
            .fileName("currentDataModelFile")
            .type(null)
            .status(DataModelFileStatus.UNCHANGED)
            .conflicted(false)
            .build(),
        DataModelFileDto.builder()
            .fileName("createTables")
            .type(DataModelFileType.TABLES_FILE)
            .status(DataModelFileStatus.CHANGED)
            .conflicted(true)
            .build()
    );
    Mockito.doReturn(dataModelChanges).when(dataModelService).listDataModelFiles(changeId);
  }

  private void mockBpList(String changeId) {
    var bpList = List.of(
        BusinessProcessInfoDto.builder()
            .name("business-process")
            .title("Really test name")
            .status(FileStatus.NEW)
            .conflicted(false)
            .build(),
        BusinessProcessInfoDto.builder()
            .name("changed-business-process")
            .title("Changed test name")
            .status(FileStatus.CHANGED)
            .conflicted(true)
            .build(),
        BusinessProcessInfoDto.builder()
            .name("unchanged-business-process")
            .title("Unchanged test name")
            .status(FileStatus.UNCHANGED)
            .conflicted(false)
            .build(),
        BusinessProcessInfoDto.builder()
            .name("deleted-business-process")
            .title("Deleted test name")
            .status(FileStatus.DELETED)
            .conflicted(false)
            .build()
    );
    Mockito.doReturn(bpList).when(businessProcessService).getChangedProcessesByVersion(changeId);
  }

  private void mockFormList(String changeId) {
    var formList = List.of(
        FormInfoDto.builder()
            .name("new_form")
            .status(FileStatus.NEW)
            .title("New Form")
            .conflicted(false)
            .build(),
        FormInfoDto.builder()
            .name("changed_form")
            .status(FileStatus.CHANGED)
            .title("Changed Form")
            .conflicted(true)
            .build(),
        FormInfoDto.builder()
            .name("Current form")
            .status(FileStatus.UNCHANGED)
            .title("Current form")
            .conflicted(false)
            .build(),
        FormInfoDto.builder()
            .name("deleted_form")
            .status(FileStatus.DELETED)
            .title("Deleted Form")
            .conflicted(false)
            .build()
    );
    Mockito.doReturn(formList).when(formService).getChangedFormsListByVersion(changeId);
  }

  private void mockI18nList(String changeId) {
    var i18nList = List.of(
        new I18nInfoDto("en", "i18n/en.json", FileStatus.NEW, false, "enEtag"),
        new I18nInfoDto("fr", "i18n/fr.json", FileStatus.CHANGED, true, "frEtag"),
        new I18nInfoDto("uk", "i18n/uk.json", FileStatus.UNCHANGED, false, "ukEtag"),
        new I18nInfoDto("pl", "i18n/pl.json", FileStatus.DELETED, false, null)
    );
    Mockito.doReturn(i18nList).when(i18nService).getChangedI18nListByVersion(changeId);
  }
}
