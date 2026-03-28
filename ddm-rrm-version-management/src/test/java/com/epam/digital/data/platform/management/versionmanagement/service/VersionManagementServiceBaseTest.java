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

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.i18n.service.I18nService;
import com.epam.digital.data.platform.management.settings.service.SettingService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.epam.digital.data.platform.management.core.event.publisher.RegistryRegulationManagementEventPublisher;
import com.epam.digital.data.platform.management.core.service.CacheService;
import com.epam.digital.data.platform.management.forms.service.FormService;
import com.epam.digital.data.platform.management.vcsintegration.service.VcsService;
import com.epam.digital.data.platform.management.gitintegration.service.JGitService;
import com.epam.digital.data.platform.management.groups.service.GroupService;
import com.epam.digital.data.platform.management.service.BusinessProcessService;
import com.epam.digital.data.platform.management.service.DataModelFileManagementService;
import com.epam.digital.data.platform.management.versionmanagement.mapper.VersionManagementMapper;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    VersionManagementServiceImpl.class
})
@ComponentScan(basePackageClasses = VersionManagementMapper.class)
@EnableConfigurationProperties
public abstract class VersionManagementServiceBaseTest {

  @Autowired
  VersionManagementServiceImpl managementService;

  @MockBean
  VcsService vcsService;
  @MockBean
  JGitService jGitService;
  @MockBean
  FormService formService;
  @MockBean
  BusinessProcessService businessProcessService;
  @MockBean
  DataModelFileManagementService dataModelService;
  @MockBean
  RegistryRegulationManagementEventPublisher publisher;
  @MockBean
  I18nService i18nService;

  @MockBean
  GroupService groupService;
  @MockBean
  CacheService cacheService;
  @MockBean
  VcsPropertiesConfig vcsPropertiesConfig;
  @MockBean
  SettingService settingService;
}
