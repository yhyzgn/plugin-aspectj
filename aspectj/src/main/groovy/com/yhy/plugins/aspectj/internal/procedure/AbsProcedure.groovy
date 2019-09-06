package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.AOPCache
import com.yhy.plugins.aspectj.internal.cache.VariantCache
import com.yhy.plugins.aspectj.internal.entity.AOPExtensionConfig

abstract class AbsProcedure {

    List<? extends AbsProcedure> procedures = new ArrayList<>()
    Project project
    AOPCache aopCache
    AOPExtensionConfig aopExtensionConfig
    VariantCache variantCache
    TransformInvocation transformInvocation

    AbsProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        this.project = project
        if (transformInvocation != null) {
            this.transformInvocation = transformInvocation
        }

        if (variantCache != null) {
            this.variantCache = variantCache
            this.aopCache = variantCache.aopCache
            this.aopExtensionConfig = aopCache.aopExtensionConfig
        }
    }

    public <T extends AbsProcedure> AbsProcedure with(T procedure) {
        if (procedure != null) {
            procedures << procedure
        }

        return this
    }

    boolean doWorkContinuously() {
        for (AbsProcedure procedure : procedures) {
            if (!procedure.doWorkContinuously()) {
                break
            }
        }
        return true
    }
}