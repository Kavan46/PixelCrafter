package com.example

import android.app.Application
import java.io.File

class PixelCrafterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Pre-create the WebView Code Cache directories to prevent chromium's opendir errors
            val jsCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!jsCacheDir.exists()) {
                jsCacheDir.mkdirs()
            }
            val wasmCacheDir = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmCacheDir.exists()) {
                wasmCacheDir.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
