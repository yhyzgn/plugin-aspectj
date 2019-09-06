package com.yhy.plugins.aspectj

import com.android.build.api.dsl.extension.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.yhy.plugins.aspectj.internal.AOPTransform
import com.yhy.plugins.aspectj.internal.TimeTrace
import org.gradle.api.Plugin
import org.gradle.api.Project

public class AOPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        project.repositories {
            mavenLocal()
        }

        project.dependencies {
            if (project.gradle.gradleVersion > "4.0") {
                project.logger.debug("gradlew version > 4.0")
                api 'org.aspectj:aspectjrt:1.9.1'
            } else {
                project.logger.debug("gradlew version < 4.0")
                compile 'org.aspectj:aspectjrt:1.9.1'
            }
        }

        project.gradle.addListener(new TimeTrace())

        //noinspection UnstableApiUsage
        AppExtension android = project.extensions.getByType(AppExtension)
        android.registerTransform(new AOPTransform(project))
    }
}