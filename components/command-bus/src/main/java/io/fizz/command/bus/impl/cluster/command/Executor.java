package io.fizz.command.bus.impl.cluster.command;

import io.fizz.common.LoggingService;
import io.fizz.common.Utils;
import io.fizz.command.bus.impl.ConfigService;
import io.vertx.core.Vertx;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Objects;
import java.util.Queue;

public class Executor {
    @FunctionalInterface
    public interface ExecutionHandler {
        void handle();
    }

    private static class TaskInfo {
        final ExecutionHandler handler;
        final Date created;

        public TaskInfo(ExecutionHandler aHandler) {
            this.handler = aHandler;
            this.created = new Date();
        }

        boolean expired(long aExpiryMs) {
            return (new Date().getTime() - created.getTime()) >= aExpiryMs;
        }
    }

    private static final LoggingService.Log log = LoggingService.getLogger(Executor.class);
    private static final int QUEUE_SIZE_THRESHOLD = 1024;
    private static final long TASK_EXPIRY_MS = ConfigService.config().getNumber("command.bus.task.expiry.ms").longValue();

    private final Queue<TaskInfo> tasks = new ArrayDeque<>();
    private final Vertx vertx;
    private long taskExpiryMs;

    public Executor(Vertx aVertx) {
        Utils.assertRequiredArgument(aVertx, "invalid vertx instance");

        this.vertx = aVertx;

        setTaskExpiryMs(TASK_EXPIRY_MS);
    }

    public void setTaskExpiryMs(long aTaskExpiryMs) {
        if (aTaskExpiryMs < 10) {
            throw new IllegalArgumentException("invalid task expiry");
        }
        this.taskExpiryMs = aTaskExpiryMs;
    }

    public void start() {
        process();
    }

    public void execute(ExecutionHandler aHandler) {
        Utils.assertRequiredArgument(aHandler, "invalid execution handler");

        if (tasks.size() >= QUEUE_SIZE_THRESHOLD) {
            log.warn("command queue size crossing threshold");
        }

        tasks.add(new TaskInfo(aHandler));
    }

    public void next() {
        tasks.poll();
        process();
    }

    public void retry() {
        scheduleProcess();
    }

    private void scheduleProcess() {
        vertx.setTimer(100, aTimerId -> process());
    }

    private void process() {
        TaskInfo taskInfo = validTaskInfo();
        if (Objects.isNull(taskInfo)) {
            scheduleProcess();
            return;
        }

        taskInfo.handler.handle();
    }

    private TaskInfo validTaskInfo() {
        TaskInfo taskInfo = tasks.peek();

        while (Objects.nonNull(taskInfo) && taskInfo.expired(taskExpiryMs)) {
            tasks.poll();
            taskInfo = tasks.peek();
        }

        return taskInfo;
    }
}
