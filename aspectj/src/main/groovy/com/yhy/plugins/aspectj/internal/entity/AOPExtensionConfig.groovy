package com.yhy.plugins.aspectj.internal.entity

import com.google.gson.annotations.SerializedName

class AOPExtensionConfig implements Serializable {

    @SerializedName("enabled")
    boolean enabled = true;

    @SerializedName("args")
    List<String> args = new ArrayList<>();

    @SerializedName("includes")
    List<String> includes = new ArrayList<>();

    @SerializedName("excludes")
    List<String> excludes = new ArrayList<>();
}