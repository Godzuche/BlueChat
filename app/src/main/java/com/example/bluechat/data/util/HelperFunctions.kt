package com.example.bluechat.data.util

import android.content.Context
import android.content.pm.PackageManager

fun Context.hasPermission(permission: String): Boolean {
    return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}