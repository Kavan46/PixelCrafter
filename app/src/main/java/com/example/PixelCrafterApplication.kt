package com.example

import android.app.Application

class PixelCrafterApplication : Application() {
    override fun getAttributionTag(): String? {
        // Return a declared attribution tag to satisfy AppOps context auditing on Android 12+
        return "attributionTag"
    }
}
