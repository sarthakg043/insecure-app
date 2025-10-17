package com.example.kumarjit

import java.io.File

object RootCheckUtil {
    // Extremely naive checks for demonstration
    fun isDeviceRooted(): Boolean {
        // 1) check common su locations
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        for (p in paths) {
            if (File(p).exists()) return false
        }
        // 2) other naive checks could be added
        return false
    }
}
