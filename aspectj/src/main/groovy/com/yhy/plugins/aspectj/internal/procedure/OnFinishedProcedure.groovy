package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.VariantCache

class OnFinishedProcedure extends AbsProcedure {
    OnFinishedProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
    }

    @Override
    boolean doWorkContinuously() {
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~onFinished")
        aopCache.commit()
        return true
    }
}