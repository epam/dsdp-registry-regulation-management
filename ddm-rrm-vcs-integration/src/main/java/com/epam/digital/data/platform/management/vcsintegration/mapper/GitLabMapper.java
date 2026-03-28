package com.epam.digital.data.platform.management.vcsintegration.mapper;

import com.epam.digital.data.platform.management.vcsintegration.model.ChangeInfoDto;
import com.epam.digital.data.platform.management.vcsintegration.model.ChangeInfoShortDto;
import com.epam.digital.data.platform.management.vcsintegration.model.FileInfoDto;
import org.apache.commons.lang3.BooleanUtils;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.MergeRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Map GitLab models to DTOs
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")

public interface GitLabMapper {

  @Mapping(target = "number", source = "iid")
  @Mapping(target = "subject", source = "title")
  @Mapping(target = "topic", source = "labels", qualifiedByName = "toTopic")
  ChangeInfoShortDto toChangeInfoShortDto(MergeRequest mergeRequest);

  @Named("toTopic")
  default String toTopic(List<String> labels) {
    return labels != null ? String.join(",", labels) : null;
  }

  @Mapping(target = "number", source = "iid")
  @Mapping(target = "changeId", source = "iid")
  @Mapping(target = "project", source = "projectId")
  @Mapping(target = "branch", source = "targetBranch")
  @Mapping(target = "owner", source = "author.username")
  @Mapping(target = "mergeable", expression = "java(!mergeRequest.getHasConflicts())")
  @Mapping(target = "refs", expression = "java(\"HEAD:refs/heads/\" + mergeRequest.getSourceBranch())")
  @Mapping(target = "labels", qualifiedByName = "toLabels")
  @Mapping(target = "subject", source = "title")
  @Mapping(target = "topic", source = "labels", qualifiedByName = "toTopic")
  @Mapping(target = "messages", expression = "java(java.util.List.of())")
  @Mapping(target = "created", source = "createdAt", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "updated", source = "updatedAt", qualifiedByName = "toLocalDateTime")
  @Mapping(target = "submitted", source = "mergedAt", qualifiedByName = "toLocalDateTime")
  ChangeInfoDto toChangeInfoDto(MergeRequest mergeRequest);

  @Named("toLabels")
  default Map<String, Integer> toLabels(List<String> labels) {
    if (labels == null || labels.isEmpty()) {
      return Map.of();
    }
    return labels.stream().collect(Collectors.toMap(Function.identity(), value -> 1));
  }

  @Named("toLocalDateTime")
  default LocalDateTime toLocalDateTime(Date date) {
    if (Objects.isNull(date)) {
      return null;
    }
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
  }

  @Mapping(target = "status", expression = "java(toFileStatus(diff))")
  @Mapping(target = "size", expression = "java(0)")
  @Mapping(target = "sizeDelta", expression = "java(0)")
  FileInfoDto toFileInfoDto(Diff diff);

  @Named("toFileStatus")
  default String toFileStatus(Diff diff) {
    if (BooleanUtils.toBoolean(diff.getNewFile())) {
      return "A"; // Added
    } else if (BooleanUtils.toBoolean(diff.getDeletedFile())) {
      return "D"; // Deleted
    } else {
      return "R"; // Modified
    }
  }
}
