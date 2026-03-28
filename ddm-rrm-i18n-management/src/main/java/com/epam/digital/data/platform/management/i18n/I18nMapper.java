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

package com.epam.digital.data.platform.management.i18n;

import com.epam.digital.data.platform.management.filemanagement.model.VersionedFileInfoDto;
import com.epam.digital.data.platform.management.i18n.model.I18nInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * The I18nMapper interface is responsible for mapping VersionedFileInfoDto, and conflicted to the
 * I18nInfoDto.
 * <p>
 * The mapping is performed using MapStruct library, with the following configurations: - The
 * unmapped target policy is set to IGNORE, which means that any properties in I18nInfoDto that are
 * not mapped from the source objects will be ignored. - The component model is set to "spring"
 * which allows the interface to be used as a Spring bean.
 * <p>
 * Example usage: var i18nInfoDto = i18nMapper.toI18n(fileInfoDto, conflicted);
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface I18nMapper {

  I18nInfoDto toI18n(VersionedFileInfoDto fileInfoDto, String eTag, boolean conflicted);
}