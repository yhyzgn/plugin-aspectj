package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.VariantCache
import com.yhy.plugins.aspectj.internal.concurrent.CallTask
import com.yhy.plugins.aspectj.internal.concurrent.ExecutorScheduler

import java.util.jar.JarEntry
import java.util.jar.JarFile

class CacheAOPFilesProcedure extends AbsProcedure {
    CacheAOPFilesProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
    }

    @Override
    boolean doWorkContinuously() {
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~cache aspect files")
        //缓存aspect文件
        ExecutorScheduler batchTaskScheduler = new ExecutorScheduler()

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                batchTaskScheduler.addTask(new CallTask() {
                    @Override
                    Object call() throws Exception {
                        dirInput.file.eachFileRecurse { File item ->
                            if (AOPUtils.isAspectClass(item)) {
                                project.logger.debug("~~~~~~~~~~~~collect aspect file:${item.absolutePath}")
                                String path = item.absolutePath
                                String subPath = path.substring(dirInput.file.absolutePath.length())
                                File cacheFile = new File(variantCache.aspectPath + subPath)
                                variantCache.add(item, cacheFile)
                            }
                        }
                        return null
                    }
                })
            }

            input.jarInputs.each { JarInput jarInput ->
                batchTaskScheduler.addTask(new CallTask() {
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
                                    project.logger.debug("~~~~~~~~~~~collect aspect file:${entryName}")
                                    variantCache.add(bytes, cacheFile)
                                }
                            }
                        }
                        jarFile.close()
                        return null
                    }
                })
            }
        }
        batchTaskScheduler.execute()
        if (AOPUtils.countOfFiles(variantCache.aspectDir) == 0) {
            AOPUtils.doWorkWithNoAspectj(transformInvocation)
            return false
        }
        return true
    }
}