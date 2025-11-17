package com.bbt.ghostroom.utils

import android.os.Build

object BuildVerUtil {
    @JvmStatic
    fun isOreoOrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 26
    }

    @JvmStatic
    fun isApi29OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }

    @JvmStatic
    fun isApi33OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 33
    }
    @JvmStatic
    fun isApi34OrAbove(): Boolean {
        return Build.VERSION.SDK_INT >= 34
    }

    @JvmStatic
    fun isApi31OrAbove(): Boolean = Build.VERSION.SDK_INT >= 31

}