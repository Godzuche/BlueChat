package com.godzuche.bluechat.core.presentation.util

import com.example.bluechat.BuildConfig

fun isDebugBuild(): Boolean {
    return BuildConfig.BUILD_TYPE == MBuildType.DEBUG.type
}

fun isReleaseBuild(): Boolean {
    return BuildConfig.BUILD_TYPE == MBuildType.RELEASE.type
}

fun currentBuildType(): String {
    return BuildConfig.BUILD_TYPE // e.g., "debug", "release", "staging"
}

enum class MBuildType(val type: String) {
    DEBUG("debug"),
    RELEASE("release"),
    STAGING("staging");
}

inline fun debugLog(generateMsg: () -> String) {
    if (isDebugBuild()) {
        println("\uD83D\uDEE0\uFE0F DEBUG: ${generateMsg()}")
    }
}
