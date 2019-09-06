package com.yhy.plugins.aspectj.internal

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

import java.util.concurrent.ConcurrentHashMap

class TimeTrace implements TaskExecutionListener, BuildListener {

    private clocks = new ConcurrentHashMap()
    private times = []
    private static final DISPLAY_TIME_THRESHOLD = 50

    @Override
    void buildStarted(Gradle gradle) {
    }

    @Override
    void settingsEvaluated(Settings settings) {

    }

    @Override
    void projectsLoaded(Gradle gradle) {

    }

    @Override
    void projectsEvaluated(Gradle gradle) {

    }

    @Override
    void buildFinished(BuildResult result) {
        LoggerFactory.getLogger(AJXPlugin).debug("Tasks spend time > ${DISPLAY_TIME_THRESHOLD}ms:")

        times.sort { lhs, rhs -> -(lhs[0] - rhs[0]) }
                .grep { it[0] > DISPLAY_TIME_THRESHOLD }
                .each { time -> printf "%14s   %s\n", formatTime(time[0]), time[1] }
    }

    @Override
    void beforeExecute(Task task) {
        clocks[task.path] = new Clock(System.currentTimeMillis())
    }

    @Override
    void afterExecute(Task task, TaskState state) {
        clocks.remove(task.path)?.with { clock ->
            def ms = clock.timeInMs
            times.add([ms, task.path])
            task.project.logger.warn("${task.path} spend ${ms}ms")
        }
    }

    static def formatTime(ms) {
        def sec = ms.intdiv(1000)
        def min = sec.intdiv(60)
        sec %= 60
        ms = (ms % 1000).intdiv(10)
        return String.format("%02d:%02d.%02d", min, sec, ms)
    }
}