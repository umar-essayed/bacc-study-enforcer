package com.baccalaureate.studyenforcer.ui.screens.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WebAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.ConcentricRingsView
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    studyRepository: StudyRepository,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(0) }
    var studentNameInput by remember { mutableStateOf("") }

    val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    val hasNotificationListener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBattery = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CanvasAlabaster)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> if (targetState > initialState) width else -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> if (targetState > initialState) -width else width } + fadeOut()
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> StepWelcomeName(
                        name = studentNameInput,
                        onNameChange = { studentNameInput = it },
                        onNext = { currentStep = 1 }
                    )
                    1 -> StepPermissions(
                        hasOverlay = hasOverlay,
                        hasNotificationListener = hasNotificationListener,
                        isIgnoringBattery = isIgnoringBattery,
                        context = context,
                        onNext = { currentStep = 2 }
                    )
                    2 -> StepReady(
                        studentName = studentNameInput.ifBlank { "بطل الثانوية" },
                        onFinish = {
                            scope.launch {
                                studyRepository.completeOnboarding(studentNameInput)
                                onComplete()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StepWelcomeName(
    name: String,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit
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
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(EmeraldLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "منظومة المتفوق",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkTitanium
                )
                Text(
                    text = "الثانوية العامة • شعبة علمي رياضة وحاسبات",
                    fontSize = 13.sp,
                    color = InkSpaceGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Text(
                text = "ما هو اسمك الكريم؟",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkTitanium
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("اكتب اسمك هنا...", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentEmerald,
                    unfocusedBorderColor = BorderHairline,
                    focusedContainerColor = SurfaceWhite,
                    unfocusedContainerColor = SurfaceWhite
                )
            )

            MinimalButton(
                text = "متابعة",
                onClick = onNext,
                variant = ButtonStyleVariant.PRIMARY,
                height = 48.dp
            )
        }
    }
}

@Composable
fun StepPermissions(
    hasOverlay: Boolean,
    hasNotificationListener: Boolean,
    isIgnoringBattery: Boolean,
    context: Context,
    onNext: () -> Unit
) {
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "صلاحيات الانضباط",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkTitanium
                )
                Text(
                    text = "تفعيل محرك الحظر والمراقبة لحجب المشتتات",
                    fontSize = 12.sp,
                    color = InkSpaceGray
                )
            }

            OnboardingPermItem(
                icon = Icons.Outlined.WebAsset,
                title = "الظهور فوق التطبيقات",
                isGranted = hasOverlay,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.startActivity(
                            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        )
                    }
                }
            )

            OnboardingPermItem(
                icon = Icons.Outlined.Shield,
                title = "إمكانية الوصول (حظر المشتتات)",
                isGranted = false,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
            )

            OnboardingPermItem(
                icon = Icons.Outlined.Notifications,
                title = "كتم إشعارات السوشيال ميديا",
                isGranted = hasNotificationListener,
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            )

            OnboardingPermItem(
                icon = Icons.Outlined.Speed,
                title = "التشغيل التلقائي (شاومي / سامسونج)",
                isGranted = false,
                onClick = {
                    try {
                        context.startActivity(Intent("miui.intent.action.OP_AUTO_START"))
                    } catch (e: Exception) {
                        try {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                            )
                        } catch (e2: Exception) {}
                    }
                }
            )

            OnboardingPermItem(
                icon = Icons.Outlined.Security,
                title = "تعطيل توفير البطارية",
                isGranted = isIgnoringBattery,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            MinimalButton(
                text = "متابعة",
                onClick = onNext,
                variant = ButtonStyleVariant.PRIMARY,
                height = 46.dp
            )
        }
    }
}

@Composable
fun OnboardingPermItem(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceSubtle)
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isGranted) EmeraldLight else SurfaceWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.Check else icon,
                contentDescription = null,
                tint = if (isGranted) AccentEmerald else InkTitanium,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = InkTitanium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isGranted) "مفعل" else "منح",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) AccentEmerald else InkSpaceGray
        )
    }
}

@Composable
fun StepReady(
    studentName: String,
    onFinish: () -> Unit
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
            ConcentricRingsView(
                size = 120.dp,
                streakProgress = 0.8f,
                focusProgress = 0.6f,
                accuracyProgress = 0.9f
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "أهلاً بك يا $studentName! 🚀",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkTitanium
                )
                Text(
                    text = "محرك الانضباط جاهز لحمايتك من التشتت وتثبيت تفوقك.",
                    fontSize = 13.sp,
                    color = InkSpaceGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            MinimalButton(
                text = "دخول المنظومة",
                onClick = onFinish,
                variant = ButtonStyleVariant.PRIMARY,
                height = 48.dp
            )
        }
    }
}
