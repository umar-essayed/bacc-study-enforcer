package com.baccalaureate.studyenforcer.ui.screens.locked

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.StudyEnforcerTheme
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenActivity : ComponentActivity() {

    @Inject
    lateinit var studyRepository: StudyRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isPenalty = intent.getBooleanExtra(EXTRA_IS_PENALTY, false)
        val reason = intent.getStringExtra(EXTRA_REASON) ?: "فترة تركيز وانضباط صارمة"

        setContent {
            StudyEnforcerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LockScreenContent(
                        isPenalty = isPenalty,
                        reason = reason,
                        studyRepository = studyRepository,
                        onUnlocked = { finish() }
                    )
                }
            }
        }
    }

    // Intercept back button to prevent leaving during lockout
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Do nothing while locked
    }

    companion object {
        const val EXTRA_IS_PENALTY = "extra_is_penalty"
        const val EXTRA_REASON = "extra_reason"
    }
}

@Composable
fun LockScreenContent(
    isPenalty: Boolean,
    reason: String,
    studyRepository: StudyRepository,
    onUnlocked: () -> Unit
) {
    val lockerState by studyRepository.getLockerStateFlow().collectAsState(initial = null)
    var remainingSeconds by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()

    var reviewMode by remember { mutableStateOf(false) }
    var reviewQuestionsSolved by remember { mutableStateOf(0) }
    var currentReviewQuestion by remember { mutableStateOf<QuestionEntity?>(null) }
    var reviewAnswerFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(lockerState) {
        while (isActive) {
            val state = lockerState
            if (state == null || !state.isActive) {
                onUnlocked()
                break
            }
            remainingSeconds = state.remainingSeconds
            delay(1000L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AppleCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isPenalty) CrimsonLight else EmeraldLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPenalty) Icons.Outlined.Warning else Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = if (isPenalty) AlertCrimson else AccentEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Title & Subtitle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isPenalty) "تطبيق محظور — وضع العقوبة نشط" else "جلسة تركيز مشددة نشطة",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = reason,
                        fontSize = 13.sp,
                        color = InkSpaceGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Apple-style Minimalist Countdown Timer
                val minutes = remainingSeconds / 60
                val seconds = remainingSeconds % 60
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceSubtle)
                        .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "%02d:%02d".format(minutes, seconds),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium,
                        letterSpacing = 1.sp
                    )
                }

                // Review Mode for Early Unlock
                if (isPenalty && !reviewMode) {
                    Text(
                        text = "يمكنك فك العقوبة مبكراً بحل 3 مراجعات نحوية متتالية بشكل صحيح.",
                        fontSize = 13.sp,
                        color = InkSpaceGray,
                        textAlign = TextAlign.Center
                    )
                    MinimalButton(
                        text = "بدء مراجعة الفك المبكر",
                        onClick = {
                            scope.launch {
                                currentReviewQuestion = studyRepository.getRandomQuestion()
                                reviewMode = true
                            }
                        },
                        variant = ButtonStyleVariant.PRIMARY
                    )
                } else if (reviewMode && currentReviewQuestion != null) {
                    val q = currentReviewQuestion!!
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "سؤال الفك (${reviewQuestionsSolved + 1}/3):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald
                        )
                        Text(
                            text = q.questionText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkTitanium
                        )

                        listOf(
                            0 to q.optionA,
                            1 to q.optionB,
                            2 to q.optionC,
                            3 to q.optionD
                        ).forEach { (idx, optText) ->
                            MinimalButton(
                                text = optText,
                                onClick = {
                                    scope.launch {
                                        if (idx == q.correctOptionIndex) {
                                            val nextCount = reviewQuestionsSolved + 1
                                            reviewQuestionsSolved = nextCount
                                            if (nextCount >= 3) {
                                                studyRepository.unlockPenaltyEarlyAfterReview()
                                                onUnlocked()
                                            } else {
                                                currentReviewQuestion = studyRepository.getRandomQuestion()
                                            }
                                        } else {
                                            reviewQuestionsSolved = 0
                                            currentReviewQuestion = studyRepository.getRandomQuestion()
                                        }
                                    }
                                },
                                variant = ButtonStyleVariant.SECONDARY,
                                height = 44.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
