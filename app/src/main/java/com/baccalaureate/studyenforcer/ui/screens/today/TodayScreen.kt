package com.baccalaureate.studyenforcer.ui.screens.today

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.ConcentricRingsView
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
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLocker: () -> Unit,
    onNavigateToSubjects: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.userStats.collectAsState()
    val subjectProgress by viewModel.subjectProgress.collectAsState()
    val lockerState by viewModel.lockerState.collectAsState()

    val streakDays = stats?.streakDays ?: 1
    val focusMins = stats?.totalFocusMinutes ?: 45
    val answered = stats?.totalAnswered ?: 0
    val correct = stats?.correctCount ?: 0
    val accuracy = if (answered > 0) (correct.toFloat() / answered.toFloat()) else 0.85f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Minimalist Header Bar
        HeaderBar(
            title = "اليوم",
            subtitle = "الثانوية العامة • علمي رياضة وحاسبات",
            trailingContent = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "الإعدادات",
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
            // Active Penalty or Locker Banner
            AnimatedVisibility(visible = lockerState?.isActive == true) {
                val isPenalty = lockerState?.isPenaltyMode == true
                AppleCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isPenalty) AlertCrimson else AccentGold,
                    backgroundColor = if (isPenalty) CrimsonLight else GoldLight
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPenalty) Icons.Outlined.Warning else Icons.Outlined.LockClock,
                            contentDescription = null,
                            tint = if (isPenalty) AlertCrimson else AccentGold,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPenalty) "وضع العقوبة مفعل (3 ضربات)" else "وضع القفل المشدد نشط",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPenalty) AlertCrimson else InkTitanium
                            )
                            val remainingMins = (lockerState?.remainingSeconds ?: 0L) / 60
                            Text(
                                text = "متبقي $remainingMins دقيقة • التطبيقات المشتتة محجوبة",
                                fontSize = 12.sp,
                                color = InkSpaceGray
                            )
                        }
                    }
                }
            }

            // Concentric Activity Rings Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConcentricRingsView(
                        size = 135.dp,
                        streakProgress = (streakDays / 10f).coerceIn(0.1f, 1f),
                        focusProgress = (focusMins / 120f).coerceIn(0.1f, 1f),
                        accuracyProgress = accuracy
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RingMetricItem(
                            label = "التتابع",
                            value = "$streakDays أيام",
                            color = AccentEmerald
                        )
                        RingMetricItem(
                            label = "التركيز",
                            value = "$focusMins دقيقة",
                            color = AccentGold
                        )
                        RingMetricItem(
                            label = "الدقة",
                            value = "${(accuracy * 100).toInt()}%",
                            color = InkTitanium
                        )
                    }
                }
            }

            // Quick Status Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Strikes Card
                val strikes = stats?.strikesCount ?: 0
                AppleCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (strikes > 0) AlertCrimson.copy(alpha = 0.5f) else BorderHairline
                ) {
                    Column {
                        Text(
                            text = "عداد الضربات",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkSpaceGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$strikes / 3",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (strikes > 0) AlertCrimson else InkTitanium
                        )
                        Text(
                            text = if (strikes == 0) "سجل نظيف" else "حذر: عقوبة عند 3",
                            fontSize = 11.sp,
                            color = if (strikes > 0) AlertCrimson else AccentEmerald
                        )
                    }
                }

                // Snooze Tokens Card
                val snoozeTokens = stats?.snoozeTokensRemaining ?: 2
                AppleCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "فرص التأجيل اليومي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkSpaceGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$snoozeTokens / 2",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (snoozeTokens > 0) AccentEmerald else InkSpaceGray
                        )
                        Text(
                            text = "تأجيل 5د للطوارئ",
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                    }
                }
            }

            // Quick Micro-Quiz Trigger Button
            MinimalButton(
                text = "⚡ بدء اختبار سريع فوري (نافذة منبثقة)",
                onClick = { viewModel.triggerQuickPopup(context) },
                variant = ButtonStyleVariant.PRIMARY
            )

            // Subject Mastery Breakdown
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مصفوفة الإتقان الأكاديمي",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkTitanium
                        )
                        Text(
                            text = "عرض الأسئلة",
                            fontSize = 12.sp,
                            color = AccentEmerald,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        )
                    }

                    val subjects = listOf(
                        "ARABIC" to ("النحو العربي (ثانية ثانوي)" to 0.88f),
                        "ENGLISH" to ("اللغة الإنجليزية (Grammar)" to 0.76f),
                        "HISTORY" to ("تاريخ مصر والشرق الأوسط" to 0.92f),
                        "PROGRAMMING" to ("البرمجة والذكاء الاصطناعي" to 0.94f)
                    )

                    subjects.forEach { (_, data) ->
                        val (name, prog) = data
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    color = InkTitanium
                                )
                                Text(
                                    text = "${(prog * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkTitanium
                                )
                            }
                            LinearProgressIndicator(
                                progress = prog,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = AccentEmerald,
                                trackColor = SurfaceSubtle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RingMetricItem(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 11.sp,
                color = InkSpaceGray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = InkTitanium
            )
        }
    }
}
