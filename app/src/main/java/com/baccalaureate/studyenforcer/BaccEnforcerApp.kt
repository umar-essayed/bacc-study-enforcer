package com.baccalaureate.studyenforcer

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.service.LocalSyncServer
import com.baccalaureate.studyenforcer.service.StudyEnforcementService
import com.google.gson.Gson
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BaccEnforcerApp : Application(), Application.ActivityLifecycleCallbacks {

    @Inject
    lateinit var studyRepository: StudyRepository

    @Inject
    lateinit var gson: Gson

    private var localSyncServer: LocalSyncServer? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        createNotificationChannel()

        // Seed initial database asynchronously
        applicationScope.launch {
            studyRepository.initializeDatabaseIfNeeded()
        }

        // Start local reverse server listener
        localSyncServer = LocalSyncServer(this, studyRepository, gson, port = 8888).apply {
            start()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                StudyEnforcementService.NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun startEnforcementService() {
        try {
            val serviceIntent = Intent(this, StudyEnforcementService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity is com.baccalaureate.studyenforcer.ui.MainActivity) {
            isAppInForeground = true
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity is com.baccalaureate.studyenforcer.ui.MainActivity) {
            isAppInForeground = false
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        var isAppInForeground: Boolean = false
            private set
    }
}
