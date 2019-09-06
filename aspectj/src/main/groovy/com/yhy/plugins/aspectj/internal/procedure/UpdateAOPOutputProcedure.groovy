package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.AOPTask
import com.yhy.plugins.aspectj.internal.AOPTaskManager
import com.yhy.plugins.aspectj.internal.cache.VariantCache

class UpdateAOPOutputProcedure extends AbsProcedure {
    AOPTaskManager aopTaskManager

    UpdateAOPOutputProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
        aopTaskManager = new AOPTaskManager(encoding: aopCache.encoding, args: aopCache.args, bootClassPath: aopCache.bootClassPath,
                sourceCompatibility: aopCache.sourceCompatibility, targetCompatibility: aopCache.targetCompatibility)
    }

    @Override
    boolean doWorkContinuously() {
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~update aspect output")
        aopTaskManager.aspectPath << variantCache.aspectDir
        aopTaskManager.classPath << variantCache.includeFileDir
        aopTaskManager.classPath << variantCache.excludeFileDir

        if (variantCache.incrementalStatus.isAspectChanged || variantCache.incrementalStatus.isIncludeFileChanged) {
            //process class files
            AOPTask aopTask = new AOPTask(project)
            File outputJar = transformInvocation.getOutputProvider().getContentLocation("include", variantCache.contentTypes, variantCache.scopes, Format.JAR)
            FileUtils.deleteQuietly(outputJar)
            aopTask.outputJar = outputJar.absolutePath
            aopTask.inPath << variantCache.includeFileDir
            aopTaskManager.addTask(aopTask)
        }

        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                aopTaskManager.classPath << jarInput.file
                File outputJar = transformInvocation.getOutputProvider().getContentLocation(jarInput.name, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR)
                if (!outputJar.getParentFile()?.exists()) {
                    outputJar.getParentFile()?.mkdirs()
                }

                if (variantCache.isIncludeJar(jarInput.file.absolutePath)) {
                    if (variantCache.incrementalStatus.isAspectChanged) {
                        FileUtils.deleteQuietly(outputJar)
                        AOPTask aopTask1 = new AOPTask(project)
                        aopTask1.inPath << jarInput.file
                        aopTask1.outputJar = outputJar.absolutePath
                        aopTaskManager.addTask(aopTask1)
                    } else {
                        if (!outputJar.exists()) {
                            AOPTask aopTask1 = new AOPTask(project)
                            aopTask1.inPath << jarInput.file
                            aopTask1.outputJar = outputJar.absolutePath
                            aopTaskManager.addTask(aopTask1)
                        }
                    }
                }
            }
        }
        aopTaskManager.batchExecute()
        return true
    }
}