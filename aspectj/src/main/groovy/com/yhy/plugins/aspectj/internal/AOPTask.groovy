package com.yhy.plugins.aspectj.internal

import com.yhy.plugins.aspectj.internal.concurrent.CallTask
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.GradleException
import org.gradle.api.Project

class AOPTask implements CallTask {

    Project project
    String encoding
    ArrayList<File> inPath = new ArrayList<>()
    ArrayList<File> aspectPath = new ArrayList<>()
    ArrayList<File> classPath = new ArrayList<>()
    List<String> args = new ArrayList<>()
    String bootClassPath
    String sourceCompatibility
    String targetCompatibility
    String outputDir
    String outputJar

    AOPTask(Project proj) {
        project = proj
    }

    @Override
    Object call() throws Exception {
        final def log = project.logger
        def args = [
                "-showWeaveInfo",
                "-encoding", encoding,
                "-source", sourceCompatibility,
                "-target", targetCompatibility,
                "-classpath", classPath.join(File.pathSeparator),
                "-bootclasspath", bootClassPath
        ]

        if (!getInPath().isEmpty()) {
            args << '-inpath'
            args << getInPath().join(File.pathSeparator)
        }
        if (!getAspectPath().isEmpty()) {
            args << '-aspectpath'
            args << getAspectPath().join(File.pathSeparator)
        }

        if (outputDir != null && !outputDir.isEmpty()) {
            args << '-d'
            args << outputDir
        }

        if (outputJar != null && !outputJar.isEmpty()) {
            args << '-outjar'
            args << outputJar
        }

        if (args != null && !args.isEmpty()) {
            if (!args.contains('-Xlint')) {
                args.add('-Xlint:ignore')
            }
            if (!args.contains('-warn')) {
                args.add('-warn:none')
            }

            args.addAll(args)
        } else {
            args.add('-Xlint:ignore')
            args.add('-warn:none')
        }

        inPath.each { File file ->
            project.logger.debug("~~~~~~~~~~~~~input file: ${file.absolutePath}")
        }

        MessageHandler handler = new MessageHandler(true)
        Main m = new Main()
        m.run(args as String[], handler)
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    throw new GradleException(message.message, message.thrown)
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break
            }
        }
        return null
    }
}