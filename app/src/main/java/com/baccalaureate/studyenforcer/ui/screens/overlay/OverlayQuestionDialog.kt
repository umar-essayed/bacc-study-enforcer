package com.baccalaureate.studyenforcer.ui.screens.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun OverlayQuestionDialog(
    question: QuestionEntity,
    currentStrikes: Int,
    snoozeTokensRemaining: Int,
    onAnswerSubmitted: (Int) -> Unit,
    onNextQuestionRequested: () -> Unit,
    onSnoozeRequested: () -> Unit,
    onDismissAfterCorrect: () -> Unit,
    onEmergencyExit: () -> Unit
) {
    var selectedIndex by remember(question.id) { mutableStateOf<Int?>(null) }
    var isSubmitted by remember(question.id) { mutableStateOf(false) }

    val isCorrect = selectedIndex != null && selectedIndex == question.correctOptionIndex
    val layoutDirection = if (question.languageRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            AppleCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)),
                cornerRadius = 22.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar: Subject + Strikes + Emergency Dismiss
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subject & Topic Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (question.lessonTitle.isNotBlank()) "${question.subject} • ${question.lessonTitle}" else "${question.subject} • ${question.topic}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkTitanium,
                                maxLines = 1
                            )
                        }

                        // Right Controls: Strikes + Snooze + Close
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (currentStrikes > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CrimsonLight)
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Warning,
                                        contentDescription = null,
                                        tint = AlertCrimson,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$currentStrikes/3",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AlertCrimson
                                    )
                                }
                            }

                            if (!isSubmitted && snoozeTokensRemaining > 0) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(onClick = onSnoozeRequested)
                                        .background(SurfaceSubtle)
                                        .border(1.dp, BorderHairline, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Timer,
                                        contentDescription = null,
                                        tint = InkSpaceGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "تأجيل 5د",
                                        fontSize = 11.sp,
                                        color = InkSpaceGray
                                    )
                                }
                            }

                            // Emergency Safe Close Button
                            IconButton(
                                onClick = onEmergencyExit,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "إغلاق",
                                    tint = InkSpaceGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Question Text
                    Text(
                        text = question.questionText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium,
                        lineHeight = 24.sp,
                        textAlign = if (question.languageRtl) TextAlign.Right else TextAlign.Left
                    )

                    // Options A, B, C, D
                    val options = listOf(
                        0 to question.optionA,
                        1 to question.optionB,
                        2 to question.optionC,
                        3 to question.optionD
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { (index, text) ->
                            val isThisSelected = selectedIndex == index
                            val isThisCorrect = question.correctOptionIndex == index

                            val (bgColor, borderColor, textColor) = when {
                                isSubmitted && isThisCorrect -> Triple(EmeraldLight, AccentEmerald, AccentEmerald)
                                isSubmitted && isThisSelected && !isThisCorrect -> Triple(CrimsonLight, AlertCrimson, AlertCrimson)
                                isThisSelected -> Triple(SurfaceSubtle, AccentEmerald, InkTitanium)
                                else -> Triple(SurfaceWhite, BorderHairline, InkTitanium)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgColor)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isSubmitted) {
                                        selectedIndex = index
                                        isSubmitted = true
                                        onAnswerSubmitted(index)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val badgeLetter = when (index) {
                                    0 -> if (question.languageRtl) "أ" else "A"
                                    1 -> if (question.languageRtl) "ب" else "B"
                                    2 -> if (question.languageRtl) "ج" else "C"
                                    else -> if (question.languageRtl) "د" else "D"
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSubmitted && isThisCorrect) AccentEmerald else SurfaceSubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSubmitted && isThisCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else if (isSubmitted && isThisSelected && !isThisCorrect) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            tint = AlertCrimson,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = badgeLetter,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkSpaceGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = text,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                    modifier = Modifier.weight(1f),
                                    textAlign = if (question.languageRtl) TextAlign.Right else TextAlign.Left
                                )
                            }
                        }
                    }

                    // Explanation Card
                    AnimatedVisibility(
                        visible = isSubmitted,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCorrect) EmeraldLight else CrimsonLight)
                                .border(1.dp, if (isCorrect) AccentEmerald.copy(alpha = 0.3f) else AlertCrimson.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isCorrect) "إجابة صحيحة ✓" else "خطأ — التفسير المعتمد:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) AccentEmerald else AlertCrimson
                            )
                            Text(
                                text = question.explanation,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = InkTitanium
                            )
                        }
                    }

                    // Bottom Action Buttons
                    if (isSubmitted) {
                        if (isCorrect) {
                            MinimalButton(
                                text = "استمرار ومتابعة",
                                onClick = onDismissAfterCorrect,
                                variant = ButtonStyleVariant.PRIMARY,
                                height = 44.dp
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalButton(
                                        text = "السؤال التالي",
                                        onClick = {
                                            selectedIndex = null
                                            isSubmitted = false
                                            onNextQuestionRequested()
                                        },
                                        variant = ButtonStyleVariant.PRIMARY,
                                        height = 44.dp
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    MinimalButton(
                                        text = "إغلاق النافذة",
                                        onClick = onEmergencyExit,
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
    }
}
