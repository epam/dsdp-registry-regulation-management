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
package com.epam.digital.data.platform.management.gitintegration.service;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.management.core.config.VcsPropertiesConfig;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.Assertions;
import net.bytebuddy.utility.RandomString;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
class JGitServiceSyncTest {

  @TempDir
  private File tempDir;

  @InjectMocks
  private JGitServiceImpl jGitService;

  @Mock
  private VcsPropertiesConfig vcsPropertiesConfig;
  @Mock
  private VcsPropertiesConfig.GerritProperties gerritProperties;
  @Mock
  private JGitWrapper jGitWrapper;
  @Mock
  private GitRetryable gitRetryable;
  @Mock
  private Git git;
  @Mock
  private Repository repository;
  @Mock
  private GitFileService gitFileService;

  @Captor
  private ArgumentCaptor<URIish> captor;

  @BeforeEach
  @SneakyThrows
  public void setUp() {
    Mockito.when(vcsPropertiesConfig.provider()).thenReturn(VcsPropertiesConfig.VcsProvider.GERRIT);
    Mockito.when(vcsPropertiesConfig.gerrit()).thenReturn(gerritProperties);
    Mockito.when(vcsPropertiesConfig.repositoryDirectory()).thenReturn(tempDir.getPath());
    Mockito.doCallRealMethod().when(gitRetryable).call(any(GitCommand.class));
  }

  @Test
  @SneakyThrows
  void resetHeadBranchToRemoteSyncTest() {
    var repoName = RandomString.make();
    var username = RandomString.make();
    var password = RandomString.make();

    var file = Path.of(tempDir.getPath(), FilenameUtils.getName(repoName)).normalize().toFile();
    Assertions.assertThat(file.createNewFile()).isTrue();

    Mockito.when(vcsPropertiesConfig.headBranch()).thenReturn(repoName);
    Mockito.when(gerritProperties.user()).thenReturn(username);
    Mockito.when(gerritProperties.password()).thenReturn(password);

    Mockito.when(jGitWrapper.open(file)).thenReturn(git);

    var fetchCommand = Mockito.mock(FetchCommand.class);
    var fetchCountDownLatch = new CountDownLatch(2);
    Mockito.when(git.fetch()).thenReturn(fetchCommand);
    Mockito.when(fetchCommand.setCredentialsProvider(
            Mockito.refEq(new UsernamePasswordCredentialsProvider(username, password))))
        .thenReturn(fetchCommand);

    var resetCommand = Mockito.mock(ResetCommand.class);
    var resetCountDownLatch = new CountDownLatch(2);
    Mockito.when(git.reset()).thenReturn(resetCommand);
    Mockito.when(resetCommand.setMode(ResetType.HARD)).thenReturn(resetCommand);
    Mockito.when(resetCommand.setRef(Constants.DEFAULT_REMOTE_NAME + "/" + repoName))
        .thenReturn(resetCommand);

    Mockito.when(fetchCommand.call()).thenAnswer(invocation -> {
      await().atMost(Duration.ofMillis(200)).until(() ->
        fetchCountDownLatch.getCount() == resetCountDownLatch.getCount()
      );
      fetchCountDownLatch.countDown();
      return null;
    });

    Mockito.when(resetCommand.call()).thenAnswer(invocation -> {
      await().atMost(Duration.ofMillis(200)).until(() ->
        fetchCountDownLatch.getCount() == resetCountDownLatch.getCount() - 1
      );
      resetCountDownLatch.countDown();
      return null;
    });

    var service = Executors.newFixedThreadPool(2);

    var firstReset = service.submit(() -> jGitService.resetHeadBranchToRemote());
    var secondReset = service.submit(() -> jGitService.resetHeadBranchToRemote());

    firstReset.get();
    Assertions.assertThat(firstReset.isDone()).isTrue();
    secondReset.get();
    Assertions.assertThat(secondReset.isDone()).isTrue();

    Assertions.assertThat(fetchCountDownLatch.getCount()).isEqualTo(0);
    Assertions.assertThat(resetCountDownLatch.getCount()).isEqualTo(0);

    Mockito.verify(fetchCommand, times(2)).setCredentialsProvider(
        Mockito.refEq(new UsernamePasswordCredentialsProvider(username, password)));
    Mockito.verify(fetchCommand, times(2)).call();

    Mockito.verify(resetCommand, times(2)).setMode(ResetType.HARD);
    Mockito.verify(resetCommand, times(2)).setRef(Constants.DEFAULT_REMOTE_NAME + "/" + repoName);
    Mockito.verify(resetCommand, times(2)).call();

    Mockito.verify(git, times(2)).close();
  }

  @Test
  @SneakyThrows
  void getFilesInPathSyncTest() {
    String version = "version";

    var file = new File(tempDir, version);
    Assertions.assertThat(file.createNewFile()).isTrue();

    String path = "forms";

    Counter counter = new Counter(2);

    when(jGitWrapper.open(file)).thenReturn(git);
    when(git.getRepository()).thenReturn(repository);
    when(vcsPropertiesConfig.headBranch()).thenReturn("master");

    TreeWalk treeWalk = mock(TreeWalk.class);
    CountDownLatch treeWalkLatch = new CountDownLatch(1);
    CountDownLatch pathStringLatch = new CountDownLatch(1);

    when(jGitWrapper.getTreeWalk(repository, path)).thenAnswer(invocation -> {
      log.info("Called retrieving the tree walker by revision for {}",
          Thread.currentThread().getName());
      counter.check(0);
      treeWalkLatch.countDown();
      return treeWalk;
    });

    when(treeWalk.next()).thenReturn(true, false, true, false,
            true, false, true, false,
            true, false)
        .thenThrow(new RuntimeException("Called too many times!"));

    when(treeWalk.getPathString()).thenAnswer(invocation -> {
      log.info("Called retrieving a ingle file from the tree for {}",
          Thread.currentThread().getName());
      counter.check(1);
      pathStringLatch.countDown();
      return "file";
    });

    int numberOfThreads = 5;
    ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      service.execute(() -> {
        try {
          jGitService.getFilesInPath(version, path);
        } catch (Exception e) {
          log.error("error", e);
          // Handle exception
        } finally {
          latch.countDown();
        }
      });
    }

    await().atMost(Duration.ofMillis(200)).until(() -> treeWalkLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> pathStringLatch.getCount() == 0);
    latch.await();

    Assertions.assertThat(counter.getCompletedIterations()).isEqualTo(numberOfThreads);
    var errorList = counter.getErrorList();
    if (!errorList.isEmpty()) {
      throw errorList.get(0);
    }
  }

  @Test
  @SneakyThrows
  void getFileContentSyncTest() {
    String version = "version";

    var file = new File(tempDir, version);
    Assertions.assertThat(file.mkdirs()).isTrue();

    String path = "forms";
    File amendFile = new File(file, path);
    Assertions.assertThat(amendFile.createNewFile()).isTrue();
    Counter counter = new Counter(1);
    CountDownLatch readFileLatch = new CountDownLatch(1);

    when(jGitWrapper.readFileContent(amendFile.toPath())).thenAnswer(invocation -> {
      log.info("Called retrieving file content from the tree for {}",
          Thread.currentThread().getName());
      counter.check(0);
      readFileLatch.countDown();
      return "content";
    });

    int numberOfThreads = 5;
    ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      service.execute(() -> {
        try {
          jGitService.getFileContent(version, path);
        } catch (Exception e) {
          log.error("error", e);
          // Handle exception
        } finally {
          latch.countDown();
        }
      });
    }

    await().atMost(Duration.ofMillis(200)).until(() -> readFileLatch.getCount() == 0);
    latch.await();

    Assertions.assertThat(counter.getCompletedIterations()).isEqualTo(numberOfThreads);
    var errorList = counter.getErrorList();
    if (!errorList.isEmpty()) {
      throw errorList.get(0);
    }
  }

  @Test
  @SneakyThrows
  void amendSyncTest() {
    final var repositoryName = RandomString.make();
    final var filepath = RandomString.make();
    final var filecontent = RandomString.make();

    var file = Path.of(tempDir.toString(), FilenameUtils.getName(repositoryName)).normalize().toFile();
    Assertions.assertThat(file.mkdirs()).isTrue();

    Counter counter = new Counter(6);

    when(jGitWrapper.open(file)).thenReturn(git);
    when(git.getRepository()).thenReturn(repository);
    when(vcsPropertiesConfig.headBranch()).thenReturn("master");
    when(gerritProperties.user()).thenReturn("user");
    when(gerritProperties.password()).thenReturn("password");
    when(gerritProperties.url()).thenReturn("https://gerrit");
    when(gerritProperties.repository()).thenReturn("repo");

    File amendedFile = Path.of(file.toString(), FilenameUtils.getName(filepath)).normalize().toFile();
    Assertions.assertThat(amendedFile.createNewFile()).isTrue();
    when(gitFileService.writeFile(repositoryName, filecontent, filepath)).thenReturn(amendedFile);

    CountDownLatch addLatch = new CountDownLatch(1);
    CountDownLatch statusLatch = new CountDownLatch(1);
    CountDownLatch logLatch = new CountDownLatch(1);
    CountDownLatch commitLatch = new CountDownLatch(1);
    CountDownLatch remoteAddLatch = new CountDownLatch(1);
    CountDownLatch pushLatch = new CountDownLatch(1);

    //add file to Git method
    AddCommand addCommand = mock(AddCommand.class);
    when(git.add()).thenReturn(addCommand);
    when(addCommand.addFilepattern(filepath)).thenReturn(addCommand);
    when(addCommand.call()).thenAnswer(invocation -> {
      log.info("Called add command for {}", Thread.currentThread().getName());
      counter.check(0);
      addLatch.countDown();
      return null;
    });

    // do amend method
    StatusCommand statusCommand = mock(StatusCommand.class);
    when(git.status()).thenReturn(statusCommand);
    Status addStatus = mock(Status.class);
    when(statusCommand.call()).thenAnswer(invocation -> {
      log.info("Called status command for {}", Thread.currentThread().getName());
      counter.check(1);
      statusLatch.countDown();
      return addStatus;
    });

    var logCommand = mock(LogCommand.class);
    when(git.log()).thenReturn(logCommand);
    final var lastCommitBuilder = new CommitBuilder();
    lastCommitBuilder.setMessage("message");
    lastCommitBuilder.setTreeId(new ObjectId(1, 2, 3, 4, 5));
    lastCommitBuilder.setAuthor(new PersonIdent("committer", "committer@epam.com",
        LocalDateTime.of(2022, 11, 10, 13, 40).toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
    lastCommitBuilder.setCommitter(lastCommitBuilder.getAuthor());
    var revCommit = RevCommit.parse(lastCommitBuilder.build());
    when(logCommand.call()).thenAnswer(invocation -> {
      log.info("Called log command for {}", Thread.currentThread().getName());
      counter.check(2);
      logLatch.countDown();
      return List.of(revCommit);
    });

    when(addStatus.isClean()).thenReturn(false);
    CommitCommand commitCommand = mock(CommitCommand.class);
    when(git.commit()).thenReturn(commitCommand);
    when(commitCommand.setMessage("message")).thenReturn(commitCommand);
    when(commitCommand.setAmend(true)).thenReturn(commitCommand);
    when(commitCommand.setAmend(true)).thenReturn(commitCommand);
    when(commitCommand.call()).thenAnswer(invocation -> {
      log.info("Called status command for {}", Thread.currentThread().getName());
      counter.check(3);
      commitLatch.countDown();
      return revCommit;
    });

    //push changes method
    RemoteAddCommand remoteAddCommand = mock(RemoteAddCommand.class);
    when(git.remoteAdd()).thenReturn(remoteAddCommand);
    when(remoteAddCommand.setName("origin")).thenReturn(remoteAddCommand);
    when(remoteAddCommand.setUri(captor.capture())).thenReturn(remoteAddCommand);
    when(remoteAddCommand.call()).thenAnswer(invocation -> {
      log.info("Called remote add command for {}", Thread.currentThread().getName());
      counter.check(4);
      remoteAddLatch.countDown();
      return null;
    });
    PushCommand pushCommand = mock(PushCommand.class);
    when(git.push()).thenReturn(pushCommand);
    when(pushCommand.setRefSpecs(refEq(new RefSpec("HEAD:refs/for/master")))).thenReturn(pushCommand);
    when(pushCommand.setCredentialsProvider(refEq(new UsernamePasswordCredentialsProvider(vcsPropertiesConfig.gerrit().user(), vcsPropertiesConfig.gerrit().password())))).thenReturn(pushCommand);
    when(pushCommand.setRemote("origin")).thenReturn(pushCommand);
    when(pushCommand.setForce(true)).thenReturn(pushCommand);
    when(pushCommand.call()).thenAnswer(invocation -> {
      log.info("Called push command for {}", Thread.currentThread().getName());
      counter.check(5);
      pushLatch.countDown();
      return null;
    });

    int numberOfThreads = 5;
    ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
    CountDownLatch latch = new CountDownLatch(numberOfThreads);

    for (int i = 0; i < numberOfThreads; i++) {
      service.execute(() -> {
        try {
          jGitService.amend(repositoryName, filepath, filecontent);
        } catch (Exception e) {
          log.error("error", e);
          // Handle exception
        } finally {
          latch.countDown();
        }
      });
    }
    await().atMost(Duration.ofMillis(200)).until(() -> addLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> statusLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> logLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> commitLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> remoteAddLatch.getCount() == 0);
    await().atMost(Duration.ofMillis(200)).until(() -> pushLatch.getCount() == 0);
    latch.await();

    Assertions.assertThat(counter.getCompletedIterations()).isEqualTo(numberOfThreads);
    var errorList = counter.getErrorList();
    if (!errorList.isEmpty()) {
      throw errorList.get(0);
    }
  }
}
