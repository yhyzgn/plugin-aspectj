package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.cache.VariantCache

class CheckAOPEnableProcedure extends AbsProcedure {

    CheckAOPEnableProcedure(Project project, VariantCache variantCache, TransformInvocation transformInvocation) {
        super(project, variantCache, transformInvocation)
    }

    @Override
    boolean doWorkContinuously() {
        project.logger.debug("~~~~~~~~~~~~~~~~~~~~~~~ check aspectjx enable")

        boolean isExcludeAll = false
        for (String filter : ajxExtensionConfig.excludes) {
            if (filter == "*" || filter == "**") {
                isExcludeAll = true
                break
            }
        }

        boolean isIncludeAll = false
        for (String filter : ajxExtensionConfig.includes) {
            if (filter == "*" || filter == "**") {
                isIncludeAll = true
                break
            }
        }

        if (isIncludeAll) {
            ajxExtensionConfig.includes.clear()
        }

        if (!ajxExtensionConfig.enabled || isExcludeAll) {
            AOPUtils.doWorkWithNoAspectj(transformInvocation)
            return false
        }
        return true
    }
}