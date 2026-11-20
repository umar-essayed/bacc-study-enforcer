package com.baccalaureate.studyenforcer.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OverlayWindowManager(
    private val context: Context,
    private val studyRepository: StudyRepository
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    var isShowing: Boolean = false
        private set

    fun showQuestionOverlay(onDismiss: (() -> Unit)? = null) {
        if (isShowing) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return
        }

        scope.launch {
            val question = studyRepository.getRandomQuestion() ?: return@launch
            val userStats = studyRepository.getUserStatsFlow()

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
                        var currentQuestion = question
                        var currentStrikesState = 0
                        var snoozeTokens = 2

                        OverlayQuestionDialog(
                            question = currentQuestion,
                            currentStrikes = currentStrikesState,
                            snoozeTokensRemaining = snoozeTokens,
                            onAnswerSubmitted = { selectedIndex ->
                                scope.launch {
                                    val result = studyRepository.recordAnswer(currentQuestion, selectedIndex)
                                    currentStrikesState = result.strikesCount
                                }
                            },
                            onNextQuestionRequested = {
                                scope.launch {
                                    val nextQ = studyRepository.getRandomQuestion()
                                    if (nextQ != null) {
                                        currentQuestion = nextQ
                                    }
                                }
                            },
                            onSnoozeRequested = {
                                scope.launch {
                                    val consumed = studyRepository.consumeSnoozeToken()
                                    if (consumed) {
                                        dismissOverlay()
                                        onDismiss?.invoke()
                                    }
                                }
                            },
                            onDismissAfterCorrect = {
                                dismissOverlay()
                                onDismiss?.invoke()
                            }
                        )
                    }
                }
            }

            try {
                windowManager.addView(composeView, params)
                owner.handleResume()
                overlayView = composeView
                isShowing = true
            } catch (e: Exception) {
                e.printStackTrace()
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
        }
    }
}
