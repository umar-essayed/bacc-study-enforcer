package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentCrimson
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun SyntaxFillScreen(
    viewModel: SyntaxFillViewModel,
    subject: String = "ALL",
    topic: String? = null,
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startGame(subject, topic)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "رجوع",
                    tint = InkTitanium
                )
            }

            // Progress text
            val total = state.questions.size
            val current = if (total > 0) state.currentIndex + 1 else 0
            Text(
                text = "السؤال $current من $total",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = InkSpaceGray
            )

            // Score Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, BorderHairline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${state.score} نقطة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.questions.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AccentEmerald,
                trackColor = BorderHairline
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (state.isComplete) {
            // Completed state
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "اكتمل التدريب!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )
                    Text(
                        text = "أحسنت! حققت ${state.score} نقطة",
                        fontSize = 14.sp,
                        color = InkSpaceGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MinimalButton(
                        text = "إعادة التدريب",
                        onClick = { viewModel.restart() },
                        style = ButtonStyleVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    MinimalButton(
                        text = "إنهاء",
                        onClick = onBackPressed,
                        style = ButtonStyleVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            val q = state.currentQuestion
            if (q != null) {
                Text(
                    text = "${q.subject} • ${q.topic}",
                    fontSize = 12.sp,
                    color = InkSpaceGray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = q.prompt,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkTitanium,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    q.options.forEachIndexed { index, option ->
                        val isSelected = state.selectedOptionIndex == index
                        val isCorrect = index == q.correctIndex
                        val isSubmitted = state.isSubmitted

                        val bgColor = when {
                            isSubmitted && isCorrect -> EmeraldLight
                            isSubmitted && isSelected && !isCorrect -> CrimsonLight
                            else -> SurfaceWhite
                        }

                        val borderColor = when {
                            isSubmitted && isCorrect -> AccentEmerald
                            isSubmitted && isSelected && !isCorrect -> AccentCrimson
                            else -> BorderHairline
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isSubmitted) {
                                    viewModel.selectOption(index)
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkTitanium,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSubmitted && isCorrect) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isSubmitted && isSelected && !isCorrect) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = null,
                                        tint = AccentCrimson,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Explanation & Next Button
                if (state.isSubmitted) {
                    Spacer(modifier = Modifier.height(16.dp))

                    AppleCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "التوضيح النحوي / العلمي:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkSpaceGray
                            )
                            Text(
                                text = q.explanation,
                                fontSize = 13.sp,
                                color = InkTitanium,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MinimalButton(
                        text = "السؤال التالي",
                        onClick = { viewModel.nextQuestion() },
                        style = ButtonStyleVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
