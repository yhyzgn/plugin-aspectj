package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.VariantCache
import com.yhy.plugins.aspectj.internal.concurrent.CallTask
import com.yhy.plugins.aspectj.internal.concurrent.ExecutorScheduler

class CacheInputFilesProcedure extends AbsProcedure {
    CacheInputFilesProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
    }

    @Override
    boolean doWorkContinuously() {
        //过滤规则
        //
        // "*" 所有class文件和jar
        // "**" 所有class文件和jar
        // "com.hujiang" 过滤 含"com.hujiang"的文件和jar
        //
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~cache input files")
        ExecutorScheduler taskScheduler = new ExecutorScheduler()

        transformInvocation.inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput dirInput ->
                variantCache.includeFileContentTypes = dirInput.contentTypes
                variantCache.includeFileScopes = dirInput.scopes

                taskScheduler.addTask(new CallTask() {
                    @Override
                    Object call() throws Exception {
                        dirInput.file.eachFileRecurse { File item ->
                            if (AOPUtils.isClassFile(item)) {
                                String path = item.absolutePath
                                String subPath = path.substring(dirInput.file.absolutePath.length())
                                String transPath = subPath.replace(File.separator, ".")

                                boolean isInclude = AOPUtils.isIncludeFilterMatched(transPath, ajxExtensionConfig.includes) &&
                                        !AOPUtils.isExcludeFilterMatched(transPath, ajxExtensionConfig.excludes)
                                variantCache.add(item, new File((isInclude ? variantCache.includeFilePath : variantCache.excludeFilePath) + subPath))
                            }
                        }
                        if (AOPUtils.countOfFiles(variantCache.excludeFileDir) > 0) {
                            File excludeJar = transformInvocation.getOutputProvider().getContentLocation("exclude", variantCache.contentTypes,
                                    variantCache.scopes, Format.JAR)
                            AOPUtils.mergeJar(variantCache.excludeFileDir, excludeJar)
                        }
                        return null
                    }
                })
            }

            input.jarInputs.each { JarInput jarInput ->
                taskScheduler.addTask(new CallTask() {
                    @Override
                    Object call() throws Exception {
                        AOPUtils.filterJar(jarInput, variantCache, ajxExtensionConfig.includes, ajxExtensionConfig.excludes)
                        if (!variantCache.isIncludeJar(jarInput.file.absolutePath)) {
                            def dest = transformInvocation.outputProvider.getContentLocation(jarInput.name
                                    , jarInput.contentTypes
                                    , jarInput.scopes
                                    , Format.JAR)
                            FileUtils.copyFile(jarInput.file, dest)
                        }
                        return null
                    }
                })
            }
        }
        taskScheduler.execute()
        variantCache.commitIncludeJarConfig()
        return true
    }
}