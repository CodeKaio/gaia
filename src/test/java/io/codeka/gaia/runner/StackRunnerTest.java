package io.codeka.gaia.runner;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import io.codeka.gaia.bo.*;
import io.codeka.gaia.repository.JobRepository;
import io.codeka.gaia.repository.StackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StackRunnerTest {

    @Mock
    private DockerClient dockerClient;

    @Mock(answer = Answers.RETURNS_SELF)
    private ContainerConfig.Builder builder;

    @Mock
    private Settings settings;

    @Mock
    private StackCommandBuilder stackCommandBuilder;

    @Mock
    private StackRepository stackRepository;

    @Mock
    private HttpHijackWorkaround httpHijackWorkaround;

    @Mock
    private JobRepository jobRepository;

    @BeforeEach
    void containerCreateMock() throws Exception {
        // simulating a container with id 12
        var containerCreation = mock(ContainerCreation.class);
        when(containerCreation.id()).thenReturn("12");
        when(dockerClient.createContainer(any())).thenReturn(containerCreation);
    }

    void httpHijackWorkaroundMock() throws Exception {
        // setting mocks to let test pass till the end
        var writableByteChannel = mock(OutputStream.class);
        when(httpHijackWorkaround.getOutputStream(any(), any())).thenReturn(writableByteChannel);
    }

    void containerExitMock(Long statusCode) throws Exception {
        // given
        var containerExit = mock(ContainerExit.class);
        when(containerExit.statusCode()).thenReturn(statusCode);
        when(dockerClient.waitContainer("12")).thenReturn(containerExit);
    }

    @Test
    void job_shouldBeSavedToDatabaseAfterRun() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildApplyScript(stack, module)).thenReturn("");

        // when
        stackRunner.apply(job, module, stack);

        // then
        verify(jobRepository).save(job);
    }

    @Test
    void successfullJob_shouldSetTheStackStateToRunning() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildApplyScript(stack, module)).thenReturn("");

        // when
        stackRunner.apply(job, module, stack);

        // then
        assertEquals(StackState.RUNNING, stack.getState());
        verify(stackRepository).save(stack);
    }

    @Test
    void plan_shouldUpdateTheStackState_whenThereIsADiffForRunningStacks() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        stack.setState(StackState.RUNNING);
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(2L);
        when(stackCommandBuilder.buildPlanScript(stack, module)).thenReturn("");

        // when
        stackRunner.plan(job, module, stack);

        // then
        verify(jobRepository).save(job);

        assertEquals(StackState.TO_UPDATE, stack.getState());
        verify(stackRepository).save(stack);
    }

    @Test
    void plan_shouldNotUpdateTheStackState_whenThereIsADiffForNewStacks() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        stack.setState(StackState.NEW);
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(2L);
        when(stackCommandBuilder.buildPlanScript(stack, module)).thenReturn("");

        // when
        stackRunner.plan(job, module, stack);

        // then
        verify(jobRepository).save(job);

        assertEquals(StackState.NEW, stack.getState());
        verifyZeroInteractions(stackRepository);
    }

    @Test
    void stop_shouldUpdateTheStackState_whenSuccessful() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        stack.setState(StackState.RUNNING);
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildDestroyScript(stack, module)).thenReturn("");

        // when
        stackRunner.stop(job, module, stack);

        // then
        verify(jobRepository).save(job);

        assertEquals(StackState.STOPPED, stack.getState());
        verify(stackRepository).save(stack);
    }

    @Test
    void jobShouldFail_whenFailingToStartContainer() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        stack.setState(StackState.RUNNING);

        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        doThrow(new DockerException("test")).when(dockerClient).startContainer("12");
        when(stackCommandBuilder.buildApplyScript(stack, module)).thenReturn("");

        // when
        stackRunner.apply(job, module, stack);

        // then
        assertEquals(JobStatus.FAILED, job.getStatus());
        verify(jobRepository).save(job);
    }

    @Test
    void plan_shouldStartPreviewJob() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildPlanScript(stack, module)).thenReturn("");

        // when
        stackRunner.plan(job, module, stack);

        // then
        assertEquals(JobType.PREVIEW, job.getType());
    }

    @Test
    void apply_shouldStartRunob() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildApplyScript(stack, module)).thenReturn("");

        // when
        stackRunner.apply(job, module, stack);

        // then
        assertEquals(JobType.RUN, job.getType());
    }

    @Test
    void stop_shouldStartStopJob() throws Exception {
        var job = new Job();
        var module = new TerraformModule();
        var stack = new Stack();
        var stackRunner = new StackRunner(dockerClient, builder, settings, stackCommandBuilder, stackRepository, httpHijackWorkaround, jobRepository);

        httpHijackWorkaroundMock();
        containerExitMock(0L);
        when(stackCommandBuilder.buildDestroyScript(stack, module)).thenReturn("");

        // when
        stackRunner.stop(job, module, stack);

        // then
        assertEquals(JobType.STOP, job.getType());
    }

}