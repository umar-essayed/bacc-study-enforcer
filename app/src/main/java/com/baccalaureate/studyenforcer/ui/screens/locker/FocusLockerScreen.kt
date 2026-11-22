package com.baccalaureate.studyenforcer.ui.screens.locker

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
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhonelinkLock
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.baccalaureate.studyenforcer.ui.theme.CrimsonLight
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun FocusLockerScreen(
    viewModel: FocusLockerViewModel,
    onNavigateToPolicy: () -> Unit
) {
    val lockerState by viewModel.lockerState.collectAsState()
    val selectedDuration by viewModel.selectedDurationMinutes.collectAsState()
    val deviceApps by viewModel.deviceApps.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val isLocked = lockerState?.isActive == true
    var showAppsList by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeaderBar(
            title = "القفل الدراسي",
            subtitle = "حجب غير قابل للإلغاء مع كتم الإشعارات لضمان التركيز"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Main Locker Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isLocked) GoldLight else SurfaceSubtle),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Outlined.Lock else Icons.Outlined.PhonelinkLock,
                            contentDescription = null,
                            tint = if (isLocked) AccentGold else InkTitanium,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    if (isLocked) {
                        val remainingSecs = lockerState?.remainingSeconds ?: 0L
                        val mins = remainingSecs / 60
                        val secs = remainingSecs % 60

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "جلسة القفل نشطة",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkTitanium,
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "%02d:%02d".format(mins, secs),
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                            Text(
                                text = "محمي من الإلغاء المبكر",
                                fontSize = 11.sp,
                                color = InkSpaceGray,
                                modifier = Modifier.padding(top = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "اختر مدة الجلسة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkTitanium,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        val durationOptions = listOf(
                            30 to "30 دقيقة",
                            60 to "ساعة",
                            120 to "ساعتان",
                            240 to "4 ساعات"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            durationOptions.forEach { (mins, label) ->
                                val isSelected = selectedDuration == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) InkTitanium else SurfaceSubtle)
                                        .border(1.dp, if (isSelected) InkTitanium else BorderHairline, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectDuration(mins) }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) SurfaceWhite else InkTitanium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        MinimalButton(
                            text = "بدء القفل ($selectedDuration دقيقة)",
                            onClick = { viewModel.activateStrictLocker() },
                            variant = ButtonStyleVariant.PRIMARY,
                            height = 46.dp
                        )
                    }
                }
            }

            // Installed Apps & Auto-Block Distractions Section
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Apps,
                                contentDescription = null,
                                tint = InkTitanium,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "إدارة التطبيقات المحظورة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkTitanium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "${deviceApps.count { it.isBlocked }} تطبيقاً محظوراً حالياً",
                                    fontSize = 11.sp,
                                    color = InkSpaceGray,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadInstalledApps() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = AccentEmerald
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "تحديث",
                                    tint = InkSpaceGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 1-Tap Auto-Block Distractions Button
                    MinimalButton(
                        text = "⚡ حظر جميع المشتتات المكتشفة تلقائياً",
                        onClick = { viewModel.autoBlockAllDistractions() },
                        variant = ButtonStyleVariant.SECONDARY,
                        height = 40.dp
                    )

                    // Toggle to expand full apps list
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showAppsList = !showAppsList }
                            .background(SurfaceSubtle)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (showAppsList) "إخفاء قائمة التطبيقات" else "عرض وتخصيص قائمة التطبيقات (${deviceApps.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkTitanium,
                            maxLines = 1,
                            softWrap = false
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = null,
                            tint = InkSpaceGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = showAppsList) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            deviceApps.forEach { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceWhite)
                                        .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = app.appName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = InkTitanium,
                                                maxLines = 1
                                            )
                                            if (app.isDistraction) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(CrimsonLight)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "مشتت",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AlertCrimson
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = app.packageName,
                                            fontSize = 10.sp,
                                            color = InkSpaceGray,
                                            maxLines = 1
                                        )
                                    }

                                    Switch(
                                        checked = app.isBlocked,
                                        onCheckedChange = { isChecked ->
                                            viewModel.toggleAppBlocked(app.packageName, isChecked)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SurfaceWhite,
                                            checkedTrackColor = AlertCrimson,
                                            uncheckedThumbColor = InkSpaceGray,
                                            uncheckedTrackColor = SurfaceSubtle
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Compact link to Policy & Rules
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToPolicy
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Policy,
                        contentDescription = null,
                        tint = InkTitanium,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "سياسة الاستخدام وقواعد القفل",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkTitanium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = BorderHairline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
