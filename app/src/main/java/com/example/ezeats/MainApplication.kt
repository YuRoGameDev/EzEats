package com.example.ezeats

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

//Application class required for the Room Storage
class ezeats : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}