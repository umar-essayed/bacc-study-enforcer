package com.baccalaureate.studyenforcer.ui.screens.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

@Composable
fun ActiveQuizScreen(
    studyRepository: StudyRepository,
    targetQuestionId: Long? = null,
    onFinished: () -> Unit
) {
    var question by remember { mutableStateOf<QuestionEntity?>(null) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isSubmitted by remember { mutableStateOf(false) }
    var currentStrikes by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val q = if (targetQuestionId != null && targetQuestionId > 0) {
            studyRepository.getRandomQuestion() // or specific
        } else {
            studyRepository.getRandomQuestion()
        }
        question = q
    }

    if (question == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(CanvasAlabaster),
            contentAlignment = Alignment.Center
        ) {
            Text("جاري تحضير السؤال...", color = InkSpaceGray)
        }
        return
    }

    val activeQ = question!!
    val isCorrect = selectedIndex != null && selectedIndex == activeQ.correctOptionIndex
    val layoutDirection = if (activeQ.languageRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasAlabaster)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            HeaderBar(
                title = "اختبار سريع",
                subtitle = "${activeQ.subject} • ${activeQ.topic}",
                trailingContent = {
                    IconButton(onClick = onFinished) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "رجوع",
                            tint = InkTitanium
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = activeQ.questionText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium,
                            lineHeight = 24.sp,
                            textAlign = if (activeQ.languageRtl) TextAlign.Right else TextAlign.Left
                        )

                        val options = listOf(
                            0 to activeQ.optionA,
                            1 to activeQ.optionB,
                            2 to activeQ.optionC,
                            3 to activeQ.optionD
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            options.forEach { (index, text) ->
                                val isThisSelected = selectedIndex == index
                                val isThisCorrect = activeQ.correctOptionIndex == index

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
                                            scope.launch {
                                                val res = studyRepository.recordAnswer(activeQ, index)
                                                currentStrikes = res.strikesCount
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val badgeLetter = when (index) {
                                        0 -> if (activeQ.languageRtl) "أ" else "A"
                                        1 -> if (activeQ.languageRtl) "ب" else "B"
                                        2 -> if (activeQ.languageRtl) "ج" else "C"
                                        else -> if (activeQ.languageRtl) "د" else "D"
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (isSubmitted && isThisCorrect) AccentEmerald else SurfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSubmitted && isThisCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = SurfaceWhite,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        } else if (isSubmitted && isThisSelected && !isThisCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                                tint = AlertCrimson,
                                                modifier = Modifier.size(15.dp)
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
                                        textAlign = if (activeQ.languageRtl) TextAlign.Right else TextAlign.Left
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(visible = isSubmitted, enter = fadeIn()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isCorrect) EmeraldLight else CrimsonLight)
                                    .border(1.dp, if (isCorrect) AccentEmerald.copy(alpha = 0.3f) else AlertCrimson.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) "إجابة صحيحة ✓" else "خطأ — التفسير النحوي:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) AccentEmerald else AlertCrimson
                                )
                                Text(
                                    text = activeQ.explanation,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = InkTitanium
                                )
                            }
                        }

                        if (isSubmitted) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                MinimalButton(
                                    text = "السؤال التالي",
                                    onClick = {
                                        scope.launch {
                                            val next = studyRepository.getRandomQuestion()
                                            if (next != null) {
                                                selectedIndex = null
                                                isSubmitted = false
                                                question = next
                                            } else {
                                                onFinished()
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    variant = ButtonStyleVariant.PRIMARY,
                                    height = 44.dp
                                )

                                MinimalButton(
                                    text = "إنهاء",
                                    onClick = onFinished,
                                    modifier = Modifier.weight(1f),
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
