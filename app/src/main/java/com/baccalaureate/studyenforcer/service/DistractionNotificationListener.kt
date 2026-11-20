package com.baccalaureate.studyenforcer.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DistractionNotificationListener : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NotificationListenerEntryPoint {
        fun studyRepository(): StudyRepository
    }

    private var studyRepository: StudyRepository? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    override fun onCreate() {
        super.onCreate()
        studyRepository = EntryPointAccessors.fromApplication(
            applicationContext,
            NotificationListenerEntryPoint::class.java
        ).studyRepository()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return
        val currentRepo = studyRepository ?: return

        scope.launch {
            val lockerState = currentRepo.getLockerState()
            if (lockerState?.isActive == true) {
                val isBlocked = currentRepo.isAppBlocked(packageName)
                if (isBlocked) {
                    // Silence distraction by canceling incoming notification banner & sound
                    cancelNotification(sbn.key)
                }
            }
        }
    }
}
