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
import java.util.Collections
import java.util.HashSet

class AppMonitorAccessibilityService : AccessibilityService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityEntryPoint {
        fun studyRepository(): StudyRepository
    }

    private var studyRepository: StudyRepository? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    // High-speed In-Memory Cache for 0ms latency synchronous interception
    private val blockedPackagesCache = Collections.synchronizedSet(HashSet<String>())
    private var isLockerActiveCached = false
    private var isPenaltyModeCached = false
    private var penaltyReasonCached = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        val repo = EntryPointAccessors.fromApplication(
            applicationContext,
            AccessibilityEntryPoint::class.java
        ).studyRepository()
        studyRepository = repo

        // Observe blocked apps and locker state into fast memory cache
        scope.launch {
            repo.getBlockedAppsFlow().collect { apps ->
                val set = apps.filter { it.isBlocked }.map { it.packageName }.toSet()
                blockedPackagesCache.clear()
                blockedPackagesCache.addAll(set)
                // Always ensure WhatsApp is in the blocked cache
                blockedPackagesCache.add("com.whatsapp")
                blockedPackagesCache.add("com.whatsapp.w4b")
            }
        }

        scope.launch {
            repo.getLockerStateFlow().collect { locker ->
                isLockerActiveCached = locker?.isActive == true
                isPenaltyModeCached = locker?.isPenaltyMode == true
                penaltyReasonCached = locker?.penaltyReason ?: ""
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        // Ignore our own application package
        if (packageName == applicationContext.packageName) return

        // 1. Anti-Cheat Self-Protection against Android Settings
        if (packageName == "com.android.settings") {
            if (isInspectingOurApp(rootInActiveWindow)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                launchLockScreen(isPenalty = isPenaltyModeCached, reason = "محاولة تجاوز القفل عبر إعدادات النظام")
                return
            }
        }

        // 2. High-Speed Interception during Focus Lock Mode
        if (isLockerActiveCached) {
            val isTargetBlocked = blockedPackagesCache.contains(packageName) ||
                    packageName.startsWith("com.whatsapp") ||
                    packageName.startsWith("com.instagram") ||
                    packageName.startsWith("com.facebook") ||
                    packageName.startsWith("com.zhiliaoapp") ||
                    packageName.startsWith("com.ss.android.ugc") ||
                    packageName.startsWith("org.telegram") ||
                    packageName.startsWith("com.twitter") ||
                    packageName.startsWith("com.snapchat")

            if (isTargetBlocked) {
                // Intercept immediately: send user HOME and launch minimalist Lock screen
                performGlobalAction(GLOBAL_ACTION_HOME)
                launchLockScreen(
                    isPenalty = isPenaltyModeCached,
                    reason = if (penaltyReasonCached.isNotBlank()) penaltyReasonCached else "وضع التركيز الصارم نشط"
                )
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
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(LockScreenActivity.EXTRA_IS_PENALTY, isPenalty)
            putExtra(LockScreenActivity.EXTRA_REASON, reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
