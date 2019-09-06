package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.*
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.VariantCache
import com.yhy.plugins.aspectj.internal.concurrent.CallTask
import com.yhy.plugins.aspectj.internal.concurrent.ExecutorScheduler

import java.util.jar.JarEntry
import java.util.jar.JarFile

class UpdateAOPFilesProcedure extends AbsProcedure {
    UpdateAOPFilesProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
    }

    @Override
    boolean doWorkContinuously() {
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~update aspect files")
        //update aspect files
        ExecutorScheduler taskScheduler = new ExecutorScheduler()

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                taskScheduler.addTask(new CallTask() {
                    @Override
                    Object call() throws Exception {
                        dirInput.changedFiles.each { File file, Status status ->
                            if (AOPUtils.isAspectClass(file)) {
                                project.logger.debug("~~~~~~~~~~~collect aspect file from Dir:${file.absolutePath}")
                                variantCache.incrementalStatus.isAspectChanged = true
                                String path = file.absolutePath
                                String subPath = path.substring(dirInput.file.absolutePath.length())
                                File cacheFile = new File(variantCache.aspectPath + subPath)
                                switch (status) {
                                    case Status.REMOVED:
                                        FileUtils.deleteQuietly(cacheFile)
                                        break
                                    case Status.CHANGED:
                                        FileUtils.deleteQuietly(cacheFile)
                                        variantCache.add(file, cacheFile)
                                        break
                                    case Status.ADDED:
                                        variantCache.add(file, cacheFile)
                                        break
                                    default:
                                        break
                                }
                            }
                        }

                        return null
                    }
                })
            }

            input.jarInputs.each { JarInput jarInput ->
                if (jarInput.status != Status.NOTCHANGED) {
                    taskScheduler.addTask(new CallTask() {
                        @Override
                        Object call() throws Exception {
                            JarFile jarFile = new JarFile(jarInput.file)
                            Enumeration<JarEntry> entries = jarFile.entries()
                            while (entries.hasMoreElements()) {
                                JarEntry jarEntry = entries.nextElement()
                                String entryName = jarEntry.getName()
                                if (!jarEntry.isDirectory() && AOPUtils.isClassFile(entryName)) {
                                    byte[] bytes = ByteStreams.toByteArray(jarFile.getInputStream(jarEntry))
                                    File cacheFile = new File(variantCache.aspectPath + File.separator + entryName)
                                    if (AOPUtils.isAspectClass(bytes)) {
                                        project.logger.debug("~~~~~~~~~~~~~~~~~collect aspect file from JAR:${entryName}")
                                        variantCache.incrementalStatus.isAspectChanged = true
                                        if (jarInput.status == Status.REMOVED) {
                                            FileUtils.deleteQuietly(cacheFile)
                                        } else if (jarInput.status == Status.CHANGED) {
                                            FileUtils.deleteQuietly(cacheFile)
                                            variantCache.add(bytes, cacheFile)
                                        } else if (jarInput.status == Status.ADDED) {
                                            variantCache.add(bytes, cacheFile)
                                        }
                                    }
                                }
                            }
                            jarFile.close()
                            return null
                        }
                    })
                }
            }
        }
        taskScheduler.execute()
        if (AOPUtils.countOfFiles(variantCache.aspectDir) == 0) {
            AOPUtils.fullCopyFiles(transformInvocation)
            return false
        }
        return true
    }
}