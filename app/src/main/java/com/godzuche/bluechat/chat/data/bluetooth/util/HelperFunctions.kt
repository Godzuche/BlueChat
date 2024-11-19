package com.godzuche.bluechat.chat.data.bluetooth.util

import android.content.Context
import android.content.pm.PackageManager

fun Context.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.haveAllPermissions(permissions: Array<String>): Boolean {
    return permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}