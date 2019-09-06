package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.AOPTask
import com.yhy.plugins.aspectj.internal.AOPTaskManager
import com.yhy.plugins.aspectj.internal.cache.VariantCache

class DoAOPWorkProcedure extends AbsProcedure {
    AOPTaskManager aopTaskManager

    DoAOPWorkProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
        aopTaskManager = new AOPTaskManager(encoding: aopCache.encoding, args: aopCache.args, bootClassPath: aopCache.bootClassPath,
                sourceCompatibility: aopCache.sourceCompatibility, targetCompatibility: aopCache.targetCompatibility)
    }

    @Override
    boolean doWorkContinuously() {
        //do aspectj real work
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~do aspectj real work")
        aopTaskManager.aspectPath << variantCache.aspectDir
        aopTaskManager.classPath << variantCache.includeFileDir
        aopTaskManager.classPath << variantCache.excludeFileDir

        AOPTask aopTask = new AOPTask(project)
        File includeJar = transformInvocation.getOutputProvider().getContentLocation("include", variantCache.contentTypes, variantCache.scopes, Format.JAR)

        if (!includeJar.parentFile.exists()) {
            FileUtils.forceMkdir(includeJar.getParentFile())
        }
        FileUtils.deleteQuietly(includeJar)
        aopTask.outputJar = includeJar.absolutePath
        aopTask.inPath << variantCache.includeFileDir
        aopTaskManager.addTask(aopTask)

        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                aopTaskManager.classPath << jarInput.file
                if (variantCache.isIncludeJar(jarInput.file.absolutePath)) {
                    AOPTask aopTask1 = new AOPTask(project)
                    aopTask1.inPath << jarInput.file
                    File outputJar = transformInvocation.getOutputProvider().getContentLocation(jarInput.name, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR)
                    if (!outputJar.getParentFile()?.exists()) {
                        outputJar.getParentFile()?.mkdirs()
                    }
                    aopTask1.outputJar = outputJar.absolutePath
                    aopTaskManager.addTask(aopTask1)
                }
            }
        }
        aopTaskManager.batchExecute()
        return true
    }
}