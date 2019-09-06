package com.yhy.plugins.aspectj.internal.cache

import com.android.build.gradle.external.cmake.server.Project
import com.yhy.plugins.aspectj.internal.AOPExtension
import com.yhy.plugins.aspectj.internal.entity.AOPExtensionConfig

class AOPCache {

    Project project
    String cachePath
    Map<String, VariantCache> variantCacheMap = new HashMap<>()

    String extensionConfigPath
    AOPExtensionConfig aopExtensionConfig = new AOPExtensionConfig()

    //for aspectj
    String encoding
    String bootClassPath
    String sourceCompatibility
    String targetCompatibility
    List<String> args = new ArrayList<>()

    AOPCache(Project proj) {
        this.project = proj
        init()
    }

    private void init() {
        cachePath = project.buildDir.absolutePath + File.separator + AndroidProject.FD_INTERMEDIATES + "/aop"
        extensionConfigPath = cachePath + File.separator + "extensionconfig.json"

        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        //extension config
        File extensionConfig = new File(extensionConfigPath)
        if (extensionConfig.exists()) {
            aopExtensionConfig = AOPUtils.optFromJsonString(FileUtils.readFileToString(extensionConfig), AJXExtensionConfig.class)
        }

        if (aopExtensionConfig == null) {
            aopExtensionConfig = new AOPExtensionConfig()
        }
    }

    File getCacheDir() {
        return new File(cachePath)
    }

    File getExtensionConfigFile() {
        return new File(extensionConfigPath)
    }

    void reset() {
        FileUtils.deleteDirectory(cacheDir)

        init()
    }

    void commit() {
        project.logger.debug("putExtensionConfig:${extensionConfigFile}")
        FileUtils.deleteQuietly(extensionConfigFile)
        File parent = extensionConfigFile.parentFile

        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        if (!extensionConfigFile.exists()) {
            extensionConfigFile.createNewFile()
        }
        String jsonString = AOPUtils.optToJsonString(aopExtensionConfig)
        project.logger.debug("${jsonString}")
        FileUtils.write(extensionConfigFile, jsonString, "UTF-8")
    }

    void put(String variantName, VariantCache cache) {
        if (variantName != null && cache != null) {
            variantCacheMap.put(variantName, cache)
        }
    }

    boolean contains(String variantName) {
        if (variantName == null) {
            return false
        }

        return variantCacheMap.containsKey(variantName)
    }

    void putExtensionConfig(AOPExtension extension) {
        if (extension == null) {
            return
        }
        aopExtensionConfig.enabled = extension.enabled
        aopExtensionConfig.args = extension.args
        aopExtensionConfig.includes = extension.includes
        aopExtensionConfig.excludes = extension.excludes
    }

    boolean isExtensionChanged(AOPExtension extension) {
        if (extension == null) {
            return true
        }
        boolean isSourceIncludesExists = aopExtensionConfig.includes != null && !aopExtensionConfig.includes.isEmpty()
        boolean isTargetIncludeExists = extension.includes != null && !extension.includes.isEmpty()
        boolean isSourceExcludeExists = aopExtensionConfig.excludes != null && !aopExtensionConfig.excludes.isEmpty()
        boolean isTargetExcludeExists = extension.excludes != null && !extension.excludes.isEmpty()
        if ((!isSourceIncludesExists && isTargetIncludeExists)
                || (isSourceIncludesExists && !isTargetIncludeExists)
                || (!isSourceExcludeExists && isTargetExcludeExists)
                || (isSourceExcludeExists && !isTargetExcludeExists)) {
            return true
        }

        if ((!isSourceIncludesExists && !isTargetIncludeExists)
                && (!isSourceExcludeExists && !isTargetExcludeExists)) {
            return false
        }

        if (aopExtensionConfig.includes.size() != extension.includes.size()
                || aopExtensionConfig.excludes.size() != extension.excludes.size()) {
            return true
        }

        boolean isChanged = false
        aopExtensionConfig.includes.each { String source ->
            boolean targetMatched = false
            for (String target : extension.includes) {
                if (source == target) {
                    targetMatched = true
                    break
                }
            }

            if (!targetMatched) {
                isChanged = true
            }
        }

        aopExtensionConfig.excludes.each { String source ->
            boolean targetMatched = false
            for (String target : extension.excludes) {
                if (source == target) {
                    targetMatched = true
                    break
                }
            }
            if (!targetMatched) {
                isChanged = true
            }
        }
        return isChanged
    }
}