/*
 * Copyright 2023 EPAM Systems.
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
package com.epam.digital.data.platform.management.vcsintegration.service;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import com.epam.digital.data.platform.management.vcsintegration.exception.VcsChangeNotFoundException;
import com.epam.digital.data.platform.management.vcsintegration.exception.VcsCommunicationException;
import com.epam.digital.data.platform.management.vcsintegration.exception.VcsConflictException;
import com.epam.digital.data.platform.management.vcsintegration.mapper.GitLabMapper;
import com.epam.digital.data.platform.management.vcsintegration.model.ChangeInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.model.ChangeInfoShortDto;
import com.epam.digital.data.platform.management.vcsintegration.model.CreateChangeInputDto;
import com.epam.digital.data.platform.management.vcsintegration.model.FileInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.model.RobotCommentInputDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.MergeRequestFilter;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.models.Constants;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GitLab implementation of GerritService interface.
 * Maps GitLab merge requests to Gerrit change concepts for backward compatibility.
 */
@Slf4j
@RequiredArgsConstructor
public class GitLabService implements VcsService {

  private final GitLabApi gitLabApi;
  private final VcsPropertiesConfig config;
  private final GitLabMapper gitLabMapper;
  private Project project;

  @PostConstruct
  public void init() throws GitLabApiException {
    this.project = gitLabApi.getProjectApi().getProject(config.gitlab().project());
  }

  @Override
  public List<ChangeInfoShortDto> getMRList() {
    try {
      log.debug("Fetching merge request list from GitLab project: {}", config.gitlab().project());

      var mergeRequests = gitLabApi.getMergeRequestApi()
          .getMergeRequests(config.gitlab().project(), Constants.MergeRequestState.OPENED);

      return mergeRequests.stream()
          .map(gitLabMapper::toChangeInfoShortDto)
          .collect(Collectors.toList());
    } catch (GitLabApiException e) {
      throw new VcsCommunicationException("Failed to fetch merge request list", e);
    }
  }

  @Override
  @Nullable
  public ChangeInfoDto getLastMergedMR() {
    try {
      log.debug("Fetching last merged MR from GitLab project: {}", config.gitlab().project());

      var filter = new MergeRequestFilter()
          .withProjectId(project.getId())
          .withState(Constants.MergeRequestState.MERGED)
          .withOrderBy(Constants.MergeRequestOrderBy.UPDATED_AT)
          .withSort(Constants.SortOrder.DESC);

      var mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(filter);

      return mergeRequests.isEmpty() ? null : gitLabMapper.toChangeInfoDto(mergeRequests.get(0));
    } catch (GitLabApiException e) {
      throw new VcsCommunicationException("Failed to fetch last merged MR", e);
    }
  }

  @Override
  public List<String> getClosedMrIds() {
    try {
      log.debug("Fetching closed MR IDs from GitLab project: {}", config.gitlab().project());

      var mergeRequests = gitLabApi.getMergeRequestApi()
          .getMergeRequests(config.gitlab().project(), Constants.MergeRequestState.MERGED);

      return mergeRequests.stream()
          .map(mr -> String.valueOf(mr.getIid()))
          .collect(Collectors.toList());
    } catch (GitLabApiException e) {
      throw new VcsCommunicationException("Failed to fetch closed MR IDs", e);
    }
  }

  @Override
  public ChangeInfoDto getMRByNumber(String number) {
    try {
      log.debug("Fetching MR by number: {} from GitLab project: {}", number, config.gitlab().project());

      var iid = Long.valueOf(number);
      var mergeRequest = gitLabApi.getMergeRequestApi().getMergeRequest(config.gitlab().project(), iid);

      return gitLabMapper.toChangeInfoDto(mergeRequest);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + number);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(number, e);
    }
  }

  @Override
  public ChangeInfoDto getChangeInfo(String changeId) {
    // In GitLab context, changeId can be MR IID
    return getMRByNumber(changeId);
  }

  @Override
  public Map<String, FileInfoDto> getListOfChangesInMR(String changeId) {
    try {
      log.debug("Fetching file changes for MR: {} from GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      var changes = gitLabApi.getMergeRequestApi()
          .getMergeRequestChanges(config.gitlab().project(), iid);

      return changes.getChanges().stream().collect(Collectors.toMap(
          diff -> diff.getNewPath() != null
              ? diff.getNewPath()
              : diff.getOldPath(), gitLabMapper::toFileInfoDto));
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public String getFileContent(String changeId, String filename) {
    try {
      log.debug("Fetching file content for MR: {}, file: {} from GitLab project: {}",
          changeId, filename, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      var mergeRequest = gitLabApi.getMergeRequestApi().getMergeRequest(config.gitlab().project(), iid);

      // Get file content from source branch
      var file = gitLabApi.getRepositoryFileApi()
          .getFile(config.gitlab().project(), filename, mergeRequest.getSourceBranch());

      return file.getDecodedContentAsString();
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void submitChanges(String changeId) {
    try {
      log.debug("Submitting MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      gitLabApi.getMergeRequestApi()
          .acceptMergeRequest(config.gitlab().project(), iid, null, true, false);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void deleteChanges(String changeId) {
    try {
      log.debug("Deleting MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      gitLabApi.getMergeRequestApi()
          .deleteMergeRequest(config.gitlab().project(), iid);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public String createChanges(CreateChangeInputDto dto) {
    try {
      log.debug("Creating MR: {} in GitLab project: {}", dto.getName(), config.gitlab().project());

      // Create a new branch for the change
      var sourceBranch = "change-" + System.currentTimeMillis();
      var targetBranch = StringUtils.hasText(config.headBranch())
          ? config.headBranch() : "main";

      // Create branch
      gitLabApi.getRepositoryApi()
          .createBranch(config.gitlab().project(), sourceBranch, targetBranch);

      // Create merge request
      var mergeRequest = gitLabApi.getMergeRequestApi()
          .createMergeRequest(
              config.gitlab().project(),
              sourceBranch,
              targetBranch,
              dto.getName(),
              dto.getDescription(),
              null
          );

      return String.valueOf(mergeRequest.getIid());
    } catch (GitLabApiException e) {
      throw new VcsCommunicationException("Failed to create MR: " + dto.getName(), e);
    }
  }

  @Override
  public Boolean review(String changeId) {
    try {
      log.debug("Reviewing MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      var mergeRequest = gitLabApi.getMergeRequestApi()
          .getMergeRequest(config.gitlab().project(), iid);

      return !mergeRequest.getHasConflicts();
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void declineChange(String changeId) {
    try {
      log.debug("Declining MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      var mergeRequest = gitLabApi.getMergeRequestApi()
          .updateMergeRequest(config.gitlab().project(), iid, null, null, null, null,
              Constants.StateEvent.CLOSE, null, null, true, null, null, null);

      gitLabApi.getRepositoryApi()
          .deleteBranch(config.gitlab().project(), mergeRequest.getSourceBranch());
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void rebase(String changeId) {
    try {
      log.debug("Rebasing MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      gitLabApi.getMergeRequestApi()
          .rebaseMergeRequest(config.gitlab().project(), iid);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void robotComment(RobotCommentInputDto requestDto, String changeId) {
    try {
      log.debug("Adding comment to MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      gitLabApi.getNotesApi()
          .createMergeRequestNote(config.gitlab().project(), iid, requestDto.getMessage(), null, null);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public String getTopic(String changeId) {
    try {
      log.debug("Getting topic for MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      var mergeRequest = gitLabApi.getMergeRequestApi()
          .getMergeRequest(config.gitlab().project(), iid);

      // In GitLab, we can use labels or milestone as topic equivalent
      if (mergeRequest.getLabels() != null && !mergeRequest.getLabels().isEmpty()) {
        return String.join(",", mergeRequest.getLabels());
      }

      return mergeRequest.getMilestone() != null ? mergeRequest.getMilestone().getTitle() : null;
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  @Override
  public void setTopic(String text, String changeId) {
    try {
      log.debug("Setting topic for MR: {} in GitLab project: {}", changeId, config.gitlab().project());

      var iid = Long.valueOf(changeId);
      gitLabApi.getMergeRequestApi()
          .updateMergeRequest(config.gitlab().project(), iid, null, null, null, null,
              null, text, null, true, null, null, null);
    } catch (NumberFormatException e) {
      throw new VcsChangeNotFoundException("Invalid MR number format: " + changeId);
    } catch (GitLabApiException e) {
      throw handleGitLabApiException(changeId, e);
    }
  }

  private static RuntimeException handleGitLabApiException(String changeId, GitLabApiException e) {
    if (e.getHttpStatus() == 404) {
      return new VcsChangeNotFoundException(String.format("MR not found: %s", changeId));
    } else if (e.getHttpStatus() == 409) {
      return new VcsConflictException(String.format("Cannot merge MR due to conflicts: %s", changeId));
    }
    return new VcsCommunicationException(String.format("Failed operation for MR: %s", changeId), e);
  }
}
