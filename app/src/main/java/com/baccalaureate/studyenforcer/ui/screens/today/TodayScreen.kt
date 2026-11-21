package com.baccalaureate.studyenforcer.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Settings
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
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToLocker: () -> Unit,
    onNavigateToSubjects: () -> Unit,
    onStartInAppQuiz: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.userStats.collectAsState()
    val lockerState by viewModel.lockerState.collectAsState()

    val studentName = stats?.userName ?: "بطل الثانوية"
    val streakDays = stats?.streakDays ?: 1
    val focusMins = stats?.totalFocusMinutes ?: 0
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
        HeaderBar(
            title = "اليوم",
            subtitle = "أهلاً بك يا $studentName 🚀",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active Penalty or Locker Banner
            AnimatedVisibility(visible = lockerState?.isActive == true) {
                val isPenalty = lockerState?.isPenaltyMode == true
                val remainingMins = (lockerState?.remainingSeconds ?: 0L) / 60
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isPenalty) "وضع العقوبة نشط" else "القفل المشدد نشط",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPenalty) AlertCrimson else InkTitanium
                            )
                            Text(
                                text = "متبقي $remainingMins دقيقة",
                                fontSize = 11.sp,
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
                        size = 125.dp,
                        streakProgress = (streakDays / 10f).coerceIn(0.1f, 1f),
                        focusProgress = (focusMins / 120f).coerceIn(0.1f, 1f),
                        accuracyProgress = accuracy
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val strikes = stats?.strikesCount ?: 0
                AppleCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (strikes > 0) AlertCrimson.copy(alpha = 0.5f) else BorderHairline
                ) {
                    Column {
                        Text(
                            text = "الضربات",
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$strikes / 3",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (strikes > 0) AlertCrimson else InkTitanium
                        )
                    }
                }

                val snoozeTokens = stats?.snoozeTokensRemaining ?: 2
                AppleCard(modifier = Modifier.weight(1f)) {
                    Column {
                        Text(
                            text = "تأجيل الطوارئ",
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$snoozeTokens / 2",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (snoozeTokens > 0) AccentEmerald else InkSpaceGray
                        )
                    }
                }
            }

            // Quick In-App Quiz Button
            MinimalButton(
                text = "اختبار فوري",
                onClick = onStartInAppQuiz,
                variant = ButtonStyleVariant.PRIMARY,
                height = 46.dp
            )

            // Subject Mastery Breakdown
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الإتقان الدراسي",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium
                        )
                        Text(
                            text = "عرض الأسئلة",
                            fontSize = 11.sp,
                            color = AccentEmerald,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clip(RoundedCornerShape(4.dp))
                        )
                    }

                    val subjects = listOf(
                        "النحو العربي" to 0.88f,
                        "English" to 0.76f,
                        "التاريخ" to 0.92f,
                        "البرمجة و AI" to 0.94f
                    )

                    subjects.forEach { (name, prog) ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
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
                                    .height(5.dp)
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
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = InkSpaceGray
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = InkTitanium
            )
        }
    }
}
