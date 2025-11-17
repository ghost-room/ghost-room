package com.bbt.ghostroom.utils

import android.annotation.SuppressLint
import android.provider.Settings

object DeviceId {
    @SuppressLint("HardwareIds")
    val id: String =
            Settings.Secure.getString(MyApp.context().contentResolver, Settings.Secure.ANDROID_ID)
}
