package com.yhy.plugins.aspectj.internal

class AOPExtension {
    List<String> includes = new ArrayList<>()
    List<String> excludes = new ArrayList<>()
    List<String> args = new ArrayList<>()

    boolean enabled = true

    AOPExtension include(String... filters) {
        if (filters != null) {
            this.includes.addAll(filters)
        }
        return this
    }

    AOPExtension exclude(String... filters) {
        if (filters != null) {
            this.excludes.addAll(filters)
        }
        return this
    }

    AOPExtension arg(String... arg) {
        if (args != null) {
            this.args.addAll(arg)
        }
        return this
    }
}