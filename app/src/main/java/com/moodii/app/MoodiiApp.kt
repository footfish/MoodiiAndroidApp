package com.moodii.app

import android.app.Application
import android.util.Log

/**
 * Main MoodiiApp application object
 */
class MoodiiApp : Application() {

    override fun onCreate() {
        Log.d("Application","Started up")
        super.onCreate()
    }
}