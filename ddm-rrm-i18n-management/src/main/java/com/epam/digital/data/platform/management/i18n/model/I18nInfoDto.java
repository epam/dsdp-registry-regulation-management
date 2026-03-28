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

package com.epam.digital.data.platform.management.i18n.model;

import com.epam.digital.data.platform.management.filemanagement.model.FileStatus;

/**
 * The I18nInfoDto class represents the data transfer object for internationalization information.
 * It contains the name, path, file status, and conflict status of an internationalized resource.
 * <p>
 * A FileStatus is an enumeration that indicates the status of the resource file. It can have one of
 * the following values: NEW, CHANGED, UNCHANGED, DELETED.</p>
 */
public record I18nInfoDto(
    String name,
    String path,
    FileStatus status,
    boolean conflicted,
    String eTag) {

}