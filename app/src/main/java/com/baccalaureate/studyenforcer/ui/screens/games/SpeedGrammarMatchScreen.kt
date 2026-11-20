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
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.AlertCrimson
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun SpeedGrammarMatchScreen(
    viewModel: SpeedGrammarViewModel,
    onBackPressed: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
            title = "طابق الإعراب السريع",
            subtitle = "المس العبارة ثم المس إعرابها الصحيح",
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
            // Live Stats Bar: 60s countdown + Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Countdown
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (state.remainingSeconds < 10) AlertCrimson.copy(alpha = 0.1f) else SurfaceSubtle)
                        .border(1.dp, if (state.remainingSeconds < 10) AlertCrimson else BorderHairline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = if (state.remainingSeconds < 10) AlertCrimson else InkTitanium,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.remainingSeconds}s",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.remainingSeconds < 10) AlertCrimson else InkTitanium
                    )
                }

                // Score Card
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

            if (state.isGameOver) {
                // Game Over Card
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "انتهت الجولة السريعة!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium
                        )
                        Text(
                            text = "النتيجة النهائية: ${state.score} نقطة",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentEmerald
                        )
                        MinimalButton(
                            text = "جولة جديدة (60 ثانية)",
                            onClick = { viewModel.startGame() },
                            variant = ButtonStyleVariant.PRIMARY
                        )
                        MinimalButton(
                            text = "العودة لقائمة التدريبات",
                            onClick = onBackPressed,
                            variant = ButtonStyleVariant.SECONDARY
                        )
                    }
                }
            } else {
                // Active Game Columns
                Text(
                    text = "1. العبارات والكلمات المحددة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkSpaceGray
                )

                // Phrases (Left Column / Top Group)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.pairs.forEach { pair ->
                        val isMatched = state.matchedIds.contains(pair.id)
                        val isSelected = state.selectedPhraseId == pair.id

                        val bgColor = when {
                            isMatched -> EmeraldLight
                            isSelected -> SurfaceSubtle
                            else -> SurfaceWhite
                        }
                        val borderColor = when {
                            isMatched -> AccentEmerald
                            isSelected -> AccentEmerald
                            else -> BorderHairline
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isMatched) { viewModel.selectPhrase(pair.id) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = pair.wordOrPhrase,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isMatched) AccentEmerald else InkTitanium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "2. الحالات والقواعد النحوية المطابقة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkSpaceGray
                )

                // Shuffled Rules (Right Column / Bottom Group)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.shuffledRules.forEach { rulePair ->
                        val isMatched = state.matchedIds.contains(rulePair.id)
                        val isSelected = state.selectedRuleId == rulePair.id

                        val bgColor = when {
                            isMatched -> EmeraldLight
                            isSelected -> SurfaceSubtle
                            else -> SurfaceWhite
                        }
                        val borderColor = when {
                            isMatched -> AccentEmerald
                            isSelected -> AccentEmerald
                            else -> BorderHairline
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isMatched) { viewModel.selectRule(rulePair.id) }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = rulePair.grammaticalRule,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isMatched) AccentEmerald else InkTitanium
                            )
                        }
                    }
                }
            }
        }
    }
}
