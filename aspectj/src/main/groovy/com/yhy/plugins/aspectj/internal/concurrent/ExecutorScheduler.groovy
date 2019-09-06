package com.yhy.plugins.aspectj.internal.concurrent

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ExecutorScheduler<T extends CallTask> {

    ExecutorService executor
    List<T> taskList

    ExecutorScheduler() {
        executor = Executors.newScheduledThreadPool(Runtime.availableProcessors() + 1)
        taskList = new ArrayList<>()
    }

    public void task(T task) {
        taskList << task
    }

    void execute() {
        executor.invokeAll(taskList)
        taskList.clear()
    }
}