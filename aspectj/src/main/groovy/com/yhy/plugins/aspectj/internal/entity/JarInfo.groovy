package com.yhy.plugins.aspectj.internal.entity

import com.google.gson.annotations.SerializedName

class JarInfo implements Serializable {

    @SerializedName("path")
    String path;
}