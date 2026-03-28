/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.digital.data.platform.management.vcsintegration.config;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.vcsintegration.mapper.GerritMapper;
import com.epam.digital.data.platform.management.vcsintegration.mapper.GitLabMapper;
import com.epam.digital.data.platform.management.vcsintegration.service.GerritService;
import com.epam.digital.data.platform.management.vcsintegration.service.GitLabService;
import com.epam.digital.data.platform.management.vcsintegration.service.VcsService;
import com.urswolfer.gerrit.client.rest.GerritApiImpl;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import org.gitlab4j.api.GitLabApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VcsConfig {

  @Bean
  @ConditionalOnProperty(name = "vcs.provider", havingValue = "gerrit", matchIfMissing = true)
  public GerritApiImpl gerritApi(VcsPropertiesConfig config) {
    return (GerritApiImpl) new GerritRestApiFactory()
        .create(new GerritAuthData.Basic(config.gerrit().url(), config.gerrit().user(), config.gerrit().password()));
  }

  @Bean
  @ConditionalOnProperty(name = "vcs.provider", havingValue = "gerrit", matchIfMissing = true)
  public VcsService gerritService(GerritApiImpl gerritApi, VcsPropertiesConfig config, GerritMapper gerritMapper) {
    return new GerritService(gerritApi, config, gerritMapper);
  }

  @Bean
  @ConditionalOnProperty(name = "vcs.provider", havingValue = "gitlab")
  public GitLabApi gitLabApi(VcsPropertiesConfig config) {
    return new GitLabApi(config.gitlab().url(), config.gitlab().token());
  }

  @Bean
  @ConditionalOnProperty(name = "vcs.provider", havingValue = "gitlab")
  public VcsService gitLabService(GitLabApi gitLabApi, VcsPropertiesConfig config, GitLabMapper gitLabMapper) {
    return new GitLabService(gitLabApi, config, gitLabMapper);
  }
}