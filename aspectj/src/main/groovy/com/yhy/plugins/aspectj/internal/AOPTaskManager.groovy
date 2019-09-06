package com.yhy.plugins.aspectj.internal

import com.yhy.plugins.aspectj.internal.concurrent.ExecutorScheduler

class AOPTaskManager {
    ArrayList<File> aspectPath = new ArrayList<>()
    ArrayList<File> classPath = new ArrayList<>()
    List<String> args = new ArrayList<>()
    String encoding
    String bootClassPath
    String sourceCompatibility
    String targetCompatibility

    ExecutorScheduler scheduler = new ExecutorScheduler()

    AOPTaskManager() {
    }


    void addTask(AOPTask task) {
        scheduler.tasks << task
    }

    void batchExecute() {
        scheduler.tasks.each { AOPTask task ->
            task.encoding = encoding
            task.aspectPath = aspectPath
            task.classPath = classPath
            task.targetCompatibility = targetCompatibility
            task.sourceCompatibility = sourceCompatibility
            task.bootClassPath = bootClassPath
            task.args = args
        }
        scheduler.execute()
    }
}