package com.baccalaureate.studyenforcer.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.baccalaureate.studyenforcer.BaccEnforcerApp
import com.baccalaureate.studyenforcer.R
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StudyEnforcementService : Service() {

    @Inject
    lateinit var studyRepository: StudyRepository

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var overlayWindowManager: OverlayWindowManager? = null
    private var screenReceiver: BroadcastReceiver? = null

    // Default interval in seconds (20 mins)
    private var popupIntervalSeconds = 20 * 60
    private var secondsUntilNextPopup = popupIntervalSeconds
    private var streakDays = 1

    override fun onCreate() {
        super.onCreate()
        overlayWindowManager = OverlayWindowManager(this, studyRepository)
        registerScreenStateReceiver()
        startForegroundWithNotification()
        startPeriodicTicker()
    }

    private fun registerScreenStateReceiver() {
        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                        // Keep live
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun startPeriodicTicker() {
        serviceScope.launch {
            launch {
                studyRepository.getUserStatsFlow().collect { stats ->
                    if (stats != null) {
                        streakDays = stats.streakDays
                    }
                }
            }

            while (isActive) {
                delay(1000L)

                val lockerState = studyRepository.getLockerState()
                val isLockerActive = lockerState?.isActive == true

                if (secondsUntilNextPopup > 0) {
                    secondsUntilNextPopup--
                } else {
                    triggerQuestion()
                    secondsUntilNextPopup = popupIntervalSeconds
                }

                if (secondsUntilNextPopup % 10 == 0 || isLockerActive) {
                    updateNotification(isLockerActive, lockerState?.remainingSeconds ?: 0L, lockerState?.isPenaltyMode == true)
                }
            }
        }
    }

    fun triggerQuestion() {
        serviceScope.launch {
            if (BaccEnforcerApp.isAppInForeground) {
                // If user is inside the app, broadcast to MainActivity to show in-app Quiz!
                val intent = Intent(ACTION_TRIGGER_IN_APP_QUIZ).apply {
                    setPackage(packageName)
                }
                sendBroadcast(intent)
            } else {
                // If user is outside the app, show WindowManager overlay!
                overlayWindowManager?.showQuestionOverlay {
                    secondsUntilNextPopup = popupIntervalSeconds
                }
            }
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification(false, 0L, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateNotification(isLocked: Boolean, remainingLockSeconds: Long, isPenalty: Boolean) {
        val notification = buildNotification(isLocked, remainingLockSeconds, isPenalty)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(isLocked: Boolean, remainingLockSeconds: Long, isPenalty: Boolean): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isPenalty -> "⚠️ وضع العقوبة نشط (3 ضربات)"
            isLocked -> "🔒 وضع القفل المشدد نشط"
            else -> "⚡ محرك الانضباط الدراسي نشط"
        }

        val contentText = when {
            isPenalty -> {
                val mins = remainingLockSeconds / 60
                "حظر التطبيقات المشتتة — متبقي $mins دقيقة"
            }
            isLocked -> {
                val mins = remainingLockSeconds / 60
                "جلسة تركيز صارمة — متبقي $mins دقيقة"
            }
            else -> {
                val minutesLeft = secondsUntilNextPopup / 60
                val secondsLeft = secondsUntilNextPopup % 60
                "سلسلة الالتزام: $streakDays أيام • الاختبار القادم: %02d:%02d".format(minutesLeft, secondsLeft)
            }
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_TRIGGER_NOW, false) == true) {
            triggerQuestion()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        overlayWindowManager?.dismissOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "study_enforcement_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_TRIGGER_NOW = "extra_trigger_now"
        const val ACTION_TRIGGER_IN_APP_QUIZ = "com.baccalaureate.studyenforcer.TRIGGER_IN_APP_QUIZ"

        fun triggerImmediateQuiz(context: Context) {
            val intent = Intent(context, StudyEnforcementService::class.java).apply {
                putExtra(EXTRA_TRIGGER_NOW, true)
            }
            context.startService(intent)
        }
    }
}
