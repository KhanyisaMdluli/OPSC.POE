package com.opsc.solowork_1

import android.app.Application
import android.content.Context
import com.opsc.solowork_1.utils.NotificationUtils

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeNotifications()
    }

    private fun initializeNotifications() {
        NotificationUtils.initializeNotificationChannels(this)
    }

    companion object {
        fun getAppContext(): Context {
            return MyApplication().applicationContext
        }
    }
}