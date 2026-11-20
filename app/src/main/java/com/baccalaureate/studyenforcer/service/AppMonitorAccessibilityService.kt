package com.baccalaureate.studyenforcer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.ui.screens.locked.LockScreenActivity
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppMonitorAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityEntryPoint {
        fun studyRepository(): StudyRepository
    }

    private var studyRepository: StudyRepository? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onServiceConnected() {
        super.onServiceConnected()
        studyRepository = EntryPointAccessors.fromApplication(
            applicationContext,
            AccessibilityEntryPoint::class.java
        ).studyRepository()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        val currentRepo = studyRepository ?: return

        // Ignore our own app package
        if (packageName == applicationContext.packageName) return

        scope.launch {
            val lockerState = currentRepo.getLockerState()
            val isLockActive = lockerState?.isActive == true

            if (!isLockActive) return@launch

            // 1. Anti-Cheat Self-Protection against Android Settings
            if (packageName == "com.android.settings") {
                if (isInspectingOurApp(rootInActiveWindow)) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                    launchLockScreen(isPenalty = lockerState?.isPenaltyMode == true, reason = "محاولة تجاوز القفل عبر إعدادات النظام")
                    return@launch
                }
            }

            // 2. Check if the app is on the blocked list
            val isBlocked = currentRepo.isAppBlocked(packageName)
            if (isBlocked) {
                // Intercept immediately: send user HOME and launch minimalist Lock screen
                performGlobalAction(GLOBAL_ACTION_HOME)
                launchLockScreen(isPenalty = lockerState?.isPenaltyMode == true, reason = lockerState?.penaltyReason ?: "")
            }
        }
    }

    /**
     * Inspects window nodes recursively to detect if user is inside this app's App Info / Settings screen
     */
    private fun isInspectingOurApp(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        val text = node.text?.toString() ?: ""
        val contentDesc = node.contentDescription?.toString() ?: ""
        val targetPkg = applicationContext.packageName

        if (text.contains(targetPkg, ignoreCase = true) ||
            contentDesc.contains(targetPkg, ignoreCase = true) ||
            text.contains("Al-Bacc", ignoreCase = true) ||
            text.contains("منظومة المتفوق", ignoreCase = true)
        ) {
            return true
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null && isInspectingOurApp(child)) {
                return true
            }
        }
        return false
    }

    private fun launchLockScreen(isPenalty: Boolean, reason: String) {
        val intent = Intent(applicationContext, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(LockScreenActivity.EXTRA_IS_PENALTY, isPenalty)
            putExtra(LockScreenActivity.EXTRA_REASON, reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
