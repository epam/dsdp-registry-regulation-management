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

/**
 * The I18nDetailsShort class represents a short version of internationalization details.
 * <p>
 * It contains the name of the i18n bundle and the eTag.
 */
public record I18nDetailsShort(
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "I18n bundle name")
    String name,
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "I18n eTag used for optimistic locking")
    String eTag
) {

}
