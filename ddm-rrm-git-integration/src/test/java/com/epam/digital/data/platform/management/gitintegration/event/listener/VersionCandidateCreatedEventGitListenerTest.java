/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.management.gitintegration.event.listener;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.core.event.VersionCandidateCreatedEvent;
import com.epam.digital.data.platform.management.gitintegration.exception.GitCommandException;
import com.epam.digital.data.platform.management.gitintegration.model.FileDatesDto;
import com.epam.digital.data.platform.management.gitintegration.service.DatesCacheService;
import com.epam.digital.data.platform.management.gitintegration.service.JGitService;
import java.util.Map;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class VersionCandidateCreatedEventGitListenerTest {

  @InjectMocks
  VersionCandidateCreatedEventGitListener listener;

  @Mock
  JGitService gitService;
  @Mock
  VcsPropertiesConfig vcsPropertiesConfig;
  @Mock
  DatesCacheService datesCacheService;

  @Test
  @SneakyThrows
  void handleVersionCandidateCreatedEvent() {
    final var source = RandomString.make();
    final var versionNumber = RandomString.make();
    final var event = new VersionCandidateCreatedEvent(source, versionNumber);

    Mockito.doNothing().when(gitService).cloneRepoIfNotExist(versionNumber);
    Mockito.doReturn("master").when(vcsPropertiesConfig).headBranch();
    Mockito.doReturn(Map.of("filePath", FileDatesDto.builder().build())).when(datesCacheService)
        .getDatesCache("master");

    listener.handleVersionCandidateCreatedEvent(event);

    Mockito.verify(gitService).cloneRepoIfNotExist(versionNumber);
    Mockito.verify(datesCacheService).getDatesCache("master");
    Mockito.verify(datesCacheService)
        .setDatesCache(versionNumber, Map.of("filePath", FileDatesDto.builder().build()));
  }

  @Test
  @SneakyThrows
  void handleVersionCandidateCreatedEvent_exceptionSuppressed() {
    final var source = RandomString.make();
    final var versionNumber = RandomString.make();
    final var event = new VersionCandidateCreatedEvent(source, versionNumber);

    Mockito.doThrow(GitCommandException.class).when(gitService).cloneRepoIfNotExist(versionNumber);

    Assertions.assertThatCode(() -> listener.handleVersionCandidateCreatedEvent(event))
        .doesNotThrowAnyException();

    Mockito.verify(gitService).cloneRepoIfNotExist(versionNumber);
  }
}
