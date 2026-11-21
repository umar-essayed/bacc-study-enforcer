package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Timer
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
fun TrueFalseScreen(
    viewModel: TrueFalseViewModel,
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

            // Timer Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (state.remainingSeconds <= 5) CrimsonLight else SurfaceWhite)
                    .border(
                        1.dp,
                        if (state.remainingSeconds <= 5) AccentCrimson else BorderHairline,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = if (state.remainingSeconds <= 5) AccentCrimson else InkSpaceGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.remainingSeconds} ثانية",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.remainingSeconds <= 5) AccentCrimson else InkTitanium
                    )
                }
            }

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

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar (30s)
        LinearProgressIndicator(
            progress = { state.remainingSeconds / 30f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (state.remainingSeconds <= 5) AccentCrimson else AccentEmerald,
            trackColor = BorderHairline
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isGameOver) {
            // Game Over Summary Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "انتهى وقت السباق!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("النقاط", fontSize = 12.sp, color = InkSpaceGray)
                            Text("${state.score}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("صحيح", fontSize = 12.sp, color = InkSpaceGray)
                            Text("${state.correctCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = InkTitanium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("خطأ", fontSize = 12.sp, color = InkSpaceGray)
                            Text("${state.wrongCount}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AccentCrimson)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    MinimalButton(
                        text = "إعادة التحدي",
                        onClick = { viewModel.restart() },
                        style = ButtonStyleVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    MinimalButton(
                        text = "إنهاء وخروج",
                        onClick = onBackPressed,
                        style = ButtonStyleVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Active Question Display
            val item = state.currentItem
            if (item != null) {
                // Topic tag
                Text(
                    text = "${item.subject} • ${item.topic}",
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
                            text = item.statement,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkTitanium,
                            lineHeight = 28.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // True / False Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // True button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EmeraldLight)
                            .border(1.5.dp, AccentEmerald, RoundedCornerShape(16.dp))
                            .clickable { viewModel.submitAnswer(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "صحيح",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                        }
                    }

                    // False button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CrimsonLight)
                            .border(1.5.dp, AccentCrimson, RoundedCornerShape(16.dp))
                            .clickable { viewModel.submitAnswer(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                tint = AccentCrimson,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "خطأ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentCrimson
                            )
                        }
                    }
                }

                // Streak badge
                if (state.streak > 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceWhite)
                            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "سلسلة صحيحة: ${state.streak} 🔥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                }
            }
        }
    }
}
