package com.yhy.plugins.aspectj.internal.procedure

import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.AOPConfig
import com.yhy.plugins.aspectj.internal.AOPExtension
import com.yhy.plugins.aspectj.internal.cache.AOPCache
import org.gradle.api.tasks.compile.JavaCompile

class AOProcedure extends AbsProcedure {

    Project project
    AOPCache aopCache

    AOProcedure(Project proj) {
        super(proj, null, null)

        project = proj
        aopCache = new AOPCache(project)

        System.setProperty("aspectj.multithreaded", "true")

        def configuration = new AOPConfig(project)

        project.afterEvaluate {
            configuration.variants.all { variant ->
                JavaCompile javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompileProvider.get() : variant.javaCompile
                aopCache.encoding = javaCompile.options.encoding
                aopCache.bootClassPath = configuration.bootClasspath.join(File.pathSeparator)
                aopCache.sourceCompatibility = javaCompile.sourceCompatibility
                aopCache.targetCompatibility = javaCompile.targetCompatibility
            }

            AOPExtension aopExtension = project.aspectjx
            //当过滤条件发生变化，clean掉编译缓存
            if (aopCache.isExtensionChanged(aopExtension)) {
                project.tasks.findByName('preBuild').dependsOn(project.tasks.findByName("clean"))
            }

            aopCache.putExtensionConfig(aopExtension)
            aopCache.args = aopExtension.args
        }

        File logDir = new File(project.buildDir.absolutePath + File.separator + "outputs" + File.separator + "logs")
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        Dump.setDumpDirectory(logDir.absolutePath)
    }
}