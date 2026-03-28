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

package com.epam.digital.data.platform.management.restapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.List;

public record VersionChangesInfo(
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed forms")
    List<EntityChangesInfo> changedForms,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed business processes")
    List<EntityChangesInfo> changedBusinessProcesses,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed data-model files")
    List<DataModelChangesInfo> changedDataModelFiles,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed groups")
    List<EntityChangesInfo> changedGroups,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed i18n bundles")
    List<EntityChangesInfo> changedI18nFiles,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "List of changed settings files")
    List<EntityChangesInfo> changedGlobalConfigurationFiles
) {

  public record EntityChangesInfo(
      @Schema(requiredMode = RequiredMode.REQUIRED, description = "Changed entity name")
      String name,
      @Schema(requiredMode = RequiredMode.REQUIRED, description = "Changed entity title")
      String title,
      @Schema(requiredMode = RequiredMode.REQUIRED, description = "Entity status. It's NEW, CHANGED or DELETED")
      ChangedFileStatus status,
      @Schema(nullable = true, description = "Is entity has conflicts")
      Boolean conflicted) {

    public enum ChangedFileStatus {
      NEW,
      CHANGED,
      DELETED
    }
  }

  public record DataModelChangesInfo(
      @Schema(requiredMode = RequiredMode.REQUIRED, description = "Data model file name")
      String name,
      @Schema(description = "Data model file type.")
      DataModelFileType fileType,
      @Schema(description = "Data model file status. It's NEW or CHANGED")
      DataModelFileStatus status,
      @Schema(nullable = true, description = "Is data model has conflicts")
      Boolean conflicted
  ) {

    public enum DataModelFileType {
      TABLES_FILE
    }

    public enum DataModelFileStatus {
      NEW,
      CHANGED
    }
  }
}
