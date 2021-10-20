package com.yhy.plugins.aspectj

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * 使用ajc编译java代码，同时织入切片代码
 * 使用 AspectJ 的编译器（ajc，一个java编译器的扩展）
 * 对所有受 aspect 影响的类进行织入。
 * 在 gradle 的编译 task 中增加额外配置，使之能正确编译运行。
 */
public class AspectJPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }

        final def logger = project.logger
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }

        project.dependencies {
            api 'org.aspectj:aspectjrt:1.9.1'
        }

        logger.error "========================";
        logger.error "Aspectj切片开始编织Class!";
        logger.error "========================";

        variants.all { variant ->
            JavaCompile javaCompile = null
            if (variant.hasProperty('javaCompileProvider')) {
                //gradle 4.10.1 +
                javaCompile = variant.javaCompileProvider.get()
            } else {
                javaCompile = variant.hasProperty('javaCompiler') ? variant.javaCompiler : variant.javaCompile
            }

            javaCompile.doLast {
                MessageHandler handler = new MessageHandler(true);

                // java
                compileJava(project, javaCompile, handler)

                // kotlin
                compileKotlin(project, javaCompile, handler, variant.buildType.name)

                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            logger.error message.message, message.thrown
                            break;
                        case IMessage.WARNING:
                            logger.warn message.message, message.thrown
                            break;
                        case IMessage.INFO:
                            logger.info message.message, message.thrown
                            break;
                        case IMessage.DEBUG:
                            logger.debug message.message, message.thrown
                            break;
                    }
                }
            }
        }
    }

    static void compileJava(Project project, JavaCompile javaCompile, MessageHandler handler) {
        String[] args = [
                "-showWeaveInfo",
                "-1.8",
                "-inpath", javaCompile.destinationDirectory.toString(),
                "-aspectpath", javaCompile.classpath.asPath,
                "-d", javaCompile.destinationDirectory.toString(),
                "-classpath", javaCompile.classpath.asPath,
                "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
        ]
        project.logger.error "ajc java args: " + Arrays.toString(args)
        handleMessage(args, handler)
    }

    static void compileKotlin(Project project, JavaCompile javaCompile, MessageHandler handler, String buildType) {
        String[] args = [
                "-showWeaveInfo",
                "-1.8",
                "-inpath", project.buildDir.path + "/tmp/kotlin-classes/" + buildType,
                "-aspectpath", javaCompile.classpath.asPath,
                "-d", project.buildDir.path + "/tmp/kotlin-classes/" + buildType,
                "-classpath", javaCompile.classpath.asPath,
                "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
        ]
        project.logger.error "ajc kotlin args: " + Arrays.toString(args)
        handleMessage(args, handler)
    }

    static void handleMessage(String[] args, MessageHandler handler) {
        new Main().run(args, handler);
    }
}