package com.linkedpipes.etl.executor.api.v1.component.task;

import com.linkedpipes.etl.executor.api.v1.report.ReportWriter;
import com.linkedpipes.etl.executor.api.v1.service.ProgressReport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TaskSourceTest {

    @Test
    public void allTasksPass() {
        TaskExecutionConfiguration configuration =
                new TaskExecutionConfiguration();
        List<TaskMock> tasks = new ArrayList<>();
        tasks.add(new TaskMock("1", "A"));
        tasks.add(new TaskMock("2", "A"));
        tasks.add(new TaskMock("3", "A"));
        TaskSource<TaskMock> source = new TaskSource<>(
                noActionProgressReport(),
                ReportWriter.noAction(), configuration, tasks);

        TaskMock first = source.getTaskWrap();
        Assertions.assertNotNull(first);
        Assertions.assertNull(source.getTaskWrap());
        source.onTaskFinished(first);

        TaskMock second = source.getTaskWrap();
        Assertions.assertNotNull(second);
        Assertions.assertNull(source.getTaskWrap());
        source.onTaskFinished(second);

        TaskMock third = source.getTaskWrap();
        Assertions.assertNotNull(third);
        Assertions.assertNull(source.getTaskWrap());
        source.onTaskFinished(third);

        Assertions.assertNull(source.getTaskWrap());
        Assertions.assertFalse(source.hasTaskExecutionFailed());
    }

    public ProgressReport noActionProgressReport() {
        return new ProgressReport() {
            @Override
            public void start(long entriesToProcess) {
                // No Action.
            }

            @Override
            public void start(Collection<?> collection) {
                // No Action.
            }

            @Override
            public void entryProcessed() {
                // No Action.
            }

            @Override
            public void done() {
                // No Action.
            }
        };
    }

    @Test
    public void failTask() {
        TaskExecutionConfiguration configuration =
                new TaskExecutionConfiguration();
        List<TaskMock> tasks = new ArrayList<>();
        tasks.add(new TaskMock("1", "A"));
        tasks.add(new TaskMock("2", "A"));
        TaskSource<TaskMock> source = new TaskSource<>(
                noActionProgressReport(),
                ReportWriter.noAction(), configuration, tasks);

        source.onTaskFinished(source.getTaskWrap());
        source.onTaskFailed(source.getTaskWrap(), null);

        Assertions.assertNull(source.getTaskWrap());
        Assertions.assertTrue(source.hasTaskExecutionFailed());
    }

    @Test
    public void retryTaskSuccess() {
        TaskExecutionConfiguration configuration =
                new TaskExecutionConfiguration();
        configuration.numberOfRetries = 2;
        List<TaskMock> tasks = new ArrayList<>();
        tasks.add(new TaskMock("1", "A"));
        TaskSource<TaskMock> source = new TaskSource<>(
                noActionProgressReport(),
                ReportWriter.noAction(), configuration, tasks);

        source.onTaskFailed(source.getTaskWrap(), null);
        source.onTaskFailed(source.getTaskWrap(), null);
        source.onTaskFinished(source.getTaskWrap());

        Assertions.assertNull(source.getTaskWrap());
        Assertions.assertFalse(source.hasTaskExecutionFailed());
    }

    @Test
    public void retryTaskFailed() {
        TaskExecutionConfiguration configuration =
                new TaskExecutionConfiguration();
        configuration.numberOfRetries = 2;
        List<TaskMock> tasks = new ArrayList<>();
        tasks.add(new TaskMock("1", "A"));
        TaskSource<TaskMock> source = new TaskSource<>(
                noActionProgressReport(),
                ReportWriter.noAction(), configuration, tasks);

        source.onTaskFailed(source.getTaskWrap(), null);
        source.onTaskFailed(source.getTaskWrap(), null);
        source.onTaskFailed(source.getTaskWrap(), null);

        Assertions.assertNull(source.getTaskWrap());
        Assertions.assertTrue(source.hasTaskExecutionFailed());
    }

}
