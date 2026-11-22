package com.baccalaureate.studyenforcer.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.ui.screens.overlay.OverlayQuestionDialog
import com.baccalaureate.studyenforcer.ui.theme.StudyEnforcerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OverlayWindowManager(
    private val context: Context,
    private val studyRepository: StudyRepository
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val currentQuestionFlow = MutableStateFlow<QuestionEntity?>(null)
    private val strikesFlow = MutableStateFlow(0)
    private val snoozeTokensFlow = MutableStateFlow(2)

    var isShowing: Boolean = false
        private set

    fun showQuestionOverlay(onDismiss: (() -> Unit)? = null) {
        if (isShowing) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        scope.launch {
            try {
                val question = studyRepository.getSmartNextQuestion() ?: return@launch
                currentQuestionFlow.value = question

                scope.launch {
                    studyRepository.getUserStatsFlow().collect { s ->
                        if (s != null) {
                            strikesFlow.value = s.strikesCount
                            snoozeTokensFlow.value = s.snoozeTokensRemaining
                        }
                    }
                }

                val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                }

                val owner = OverlayLifecycleOwner()
                lifecycleOwner = owner

                val composeView = ComposeView(context).apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                    setViewTreeLifecycleOwner(owner)
                    setViewTreeSavedStateRegistryOwner(owner)
                    setViewTreeViewModelStoreOwner(owner)

                    setContent {
                        StudyEnforcerTheme {
                            val activeQuestion by currentQuestionFlow.collectAsState()
                            val strikes by strikesFlow.collectAsState()
                            val snoozeTokens by snoozeTokensFlow.collectAsState()

                            if (activeQuestion != null) {
                                OverlayQuestionDialog(
                                    question = activeQuestion!!,
                                    currentStrikes = strikes,
                                    snoozeTokensRemaining = snoozeTokens,
                                    onAnswerSubmitted = { selectedIndex ->
                                        scope.launch {
                                            try {
                                                val result = studyRepository.recordAnswer(activeQuestion!!, selectedIndex)
                                                strikesFlow.value = result.strikesCount
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    },
                                    onNextQuestionRequested = {
                                        scope.launch {
                                            try {
                                                val nextQ = studyRepository.getSmartNextQuestion(excludeId = activeQuestion?.id)
                                                if (nextQ != null && nextQ.id != activeQuestion?.id) {
                                                    currentQuestionFlow.value = nextQ
                                                } else {
                                                    dismissOverlay()
                                                    onDismiss?.invoke()
                                                }
                                            } catch (e: Exception) {
                                                dismissOverlay()
                                                onDismiss?.invoke()
                                            }
                                        }
                                    },
                                    onSnoozeRequested = {
                                        scope.launch {
                                            try {
                                                val consumed = studyRepository.consumeSnoozeToken()
                                                if (consumed) {
                                                    dismissOverlay()
                                                    onDismiss?.invoke()
                                                }
                                            } catch (e: Exception) {
                                                dismissOverlay()
                                                onDismiss?.invoke()
                                            }
                                        }
                                    },
                                    onDismissAfterCorrect = {
                                        dismissOverlay()
                                        onDismiss?.invoke()
                                    },
                                    onEmergencyExit = {
                                        dismissOverlay()
                                        onDismiss?.invoke()
                                    }
                                )
                            }
                        }
                    }
                }

                windowManager.addView(composeView, params)
                owner.handleResume()
                overlayView = composeView
                isShowing = true
            } catch (e: Exception) {
                e.printStackTrace()
                dismissOverlay()
            }
        }
    }

    fun dismissOverlay() {
        if (!isShowing) return
        try {
            lifecycleOwner?.handlePause()
            lifecycleOwner?.handleDestroy()
            overlayView?.let { windowManager.removeView(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            overlayView = null
            lifecycleOwner = null
            isShowing = false
            currentQuestionFlow.value = null
        }
    }
}
