package com.yhy.plugins.aspectj.internal

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.android.build.gradle.internal.pipeline.TransformTask
import com.yhy.plugins.aspectj.internal.cache.VariantCache
import com.yhy.plugins.aspectj.internal.procedure.*
import org.gradle.api.internal.artifacts.transform.TransformException

class AOPTransform extends Transform {

    AOProcedure aoProcedure

    AOPTransform(Project proj) {
        aoProcedure = new AOProcedure(proj)
    }

    @Override
    String getName() {
        return "aop"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.<QualifiedContent.ContentType> of(QualifiedContent.DefaultContentType.CLASSES)
    }

    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        //是否支持增量编译
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {

        Project project = aoProcedure.project

        TransformTask transformTask = (TransformTask) transformInvocation.context
        VariantCache variantCache = new VariantCache(aoProcedure.project, aoProcedure.aopCache, transformTask.variantName)

        aoProcedure.with(new CheckAOPEnableProcedure(project, variantCache, transformInvocation))

        if (transformInvocation.incremental) {
            //incremental build
            aoProcedure.with(new UpdateAOPFilesProcedure(project, variantCache, transformInvocation))
            aoProcedure.with(new UpdateInputFilesProcedure(project, variantCache, transformInvocation))
            aoProcedure.with(new UpdateAOPOutputProcedure(project, variantCache, transformInvocation))
        } else {
            //delete output and cache before full build
            transformInvocation.outputProvider.deleteAll()
            variantCache.reset()

            aoProcedure.with(new CacheAOPFilesProcedure(project, variantCache, transformInvocation))
            aoProcedure.with(new CacheInputFilesProcedure(project, variantCache, transformInvocation))
            aoProcedure.with(new DoAOPWorkProcedure(project, variantCache, transformInvocation))
        }

        aoProcedure.with(new OnFinishedProcedure(project, variantCache, transformInvocation))

        aoProcedure.doWorkContinuously()
    }
}

