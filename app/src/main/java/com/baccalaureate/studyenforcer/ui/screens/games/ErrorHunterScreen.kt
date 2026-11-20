package com.baccalaureate.studyenforcer.ui.screens.games

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun ErrorHunterScreen(
    viewModel: ErrorHunterViewModel,
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val q = state.currentQuestion

    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        HeaderBar(
            title = "صائد الأخطاء النحوية",
            subtitle = "المس الجزء الذي يحتوي على خطأ إعرابي خلال 10 ثوانٍ",
            trailingContent = {
                IconButton(onClick = onBackPressed) {
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
            // Live 10s Timer Bar + Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.remainingSeconds < 4) AlertCrimson.copy(alpha = 0.1f) else SurfaceSubtle)
                        .border(1.dp, if (state.remainingSeconds < 4) AlertCrimson else BorderHairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = if (state.remainingSeconds < 4) AlertCrimson else InkTitanium,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.remainingSeconds} ثوانٍ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.remainingSeconds < 4) AlertCrimson else InkTitanium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldLight)
                        .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "النقاط: ${state.score}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }
            }

            // 10s Depleting Progress Bar
            LinearProgressIndicator(
                progress = (state.remainingSeconds / 10f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (state.remainingSeconds < 4) AlertCrimson else AccentEmerald,
                trackColor = SurfaceSubtle
            )

            if (q != null) {
                val direction = if (q.languageRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    AppleCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "السؤال ${state.currentIndex + 1} من ${state.questions.size}:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkSpaceGray
                            )

                            Text(
                                text = "المس المقطع الخاطئ من الجملة الآتية:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkTitanium
                            )

                            // 3 Segments: A, B, C
                            val segments = listOf(
                                0 to q.segmentA,
                                1 to q.segmentB,
                                2 to q.segmentC
                            )

                            segments.forEach { (index, segmentText) ->
                                val isSelected = state.selectedSegment == index
                                val isBroken = q.brokenSegmentIndex == index

                                val (bgColor, borderColor) = when {
                                    state.isSubmitted && isBroken -> Pair(EmeraldLight, AccentEmerald)
                                    state.isSubmitted && isSelected && !isBroken -> Pair(CrimsonLight, AlertCrimson)
                                    else -> Pair(SurfaceWhite, BorderHairline)
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(bgColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable(enabled = !state.isSubmitted) {
                                            viewModel.selectSegment(index)
                                        }
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceSubtle),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (index) {
                                                0 -> if (q.languageRtl) "أ" else "A"
                                                1 -> if (q.languageRtl) "ب" else "B"
                                                else -> if (q.languageRtl) "ج" else "C"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkSpaceGray
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = segmentText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = InkTitanium
                                    )
                                }
                            }

                            // Explanation Card
                            AnimatedVisibility(
                                visible = state.isSubmitted,
                                enter = fadeIn()
                            ) {
                                val isCorrectCatch = state.selectedSegment == q.brokenSegmentIndex
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isCorrectCatch) EmeraldLight else CrimsonLight)
                                        .border(1.dp, if (isCorrectCatch) AccentEmerald.copy(alpha = 0.3f) else AlertCrimson.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isCorrectCatch) "صيد متقن! (+100)" else "لم يتم صيد الخطأ!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCorrectCatch) AccentEmerald else AlertCrimson
                                    )
                                    Text(
                                        text = q.explanation,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = InkTitanium
                                    )
                                    Text(
                                        text = "الصواب: ${q.correctedText}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AccentEmerald
                                    )
                                }
                            }

                            if (state.isSubmitted) {
                                MinimalButton(
                                    text = "السؤال التالي",
                                    onClick = { viewModel.nextQuestion() },
                                    variant = ButtonStyleVariant.PRIMARY
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
