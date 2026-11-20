package com.baccalaureate.studyenforcer.ui.screens.onboarding

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.WebAsset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.StudyEnforcerTheme
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

class PermissionsOnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyEnforcerTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    PermissionsScreen(onBackPressed = { finish() })
                }
            }
        }
    }
}

@Composable
fun PermissionsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    var refreshTrigger by remember { mutableStateOf(0) }

    val hasOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
    val hasNotificationListener = NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBattery = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        HeaderBar(
            title = "جاهزية النظام والصلاحيات",
            subtitle = "متطلبات الحماية القصوى على Xiaomi / HyperOS / Samsung",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Step 1: Overlay Permission
            PermissionItemCard(
                icon = Icons.Outlined.WebAsset,
                title = "الظهور فوق التطبيقات الأخرى",
                description = "مطلوب لعرض نوافذ الأسئلة التلقائية وحجب الشاشة أثناء جلسات المذاكرة.",
                isGranted = hasOverlay,
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                }
            )

            // Step 2: Accessibility Service
            PermissionItemCard(
                icon = Icons.Outlined.Shield,
                title = "إمكانية الوصول (Accessibility Service)",
                description = "محرك المراقبة لاعتراض فتح تطبيقات التواصل الاجتماعي المحظورة وحماية التطبيق من الإيقاف الإجباري.",
                isGranted = false, // Must be checked in system settings
                onAction = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )

            // Step 3: Notification Listener
            PermissionItemCard(
                icon = Icons.Outlined.Notifications,
                title = "الوصول إلى الإشعارات (كتم المشتتات)",
                description = "إسكات إشعارات ومنشورات السوشيال ميديا تلقائياً فور بدء وضع العقوبة أو القفل المشدد.",
                isGranted = hasNotificationListener,
                onAction = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    context.startActivity(intent)
                }
            )

            // Step 4: HyperOS / MIUI Autostart
            PermissionItemCard(
                icon = Icons.Outlined.Speed,
                title = "التشغيل التلقائي (HyperOS / MIUI)",
                description = "ضمان استمرار عمل محرك الانضباط في الخلفية دون قتله من نظام إدارة الذاكرة.",
                isGranted = false,
                onAction = {
                    try {
                        val intent = Intent("miui.intent.action.OP_AUTO_START").apply {
                            addCategory(Intent.CATEGORY_DEFAULT)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val intent = Intent().apply {
                                component = ComponentName(
                                    "com.miui.securitycenter",
                                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                                )
                            }
                            context.startActivity(intent)
                        } catch (e2: Exception) {
                            // Fallback to app info settings
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            )

            // Step 5: Ignore Battery Optimization
            PermissionItemCard(
                icon = Icons.Outlined.Security,
                title = "تعطيل تحسين استهلاك البطارية",
                description = "يمنع أندرويد من وضع التطبيق في وضع السكون (Doze) أثناء الجلسات الدراسية.",
                isGranted = isIgnoringBattery,
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            MinimalButton(
                text = "اكتمل الضبط — العودة للرئيسية",
                onClick = onBackPressed,
                variant = ButtonStyleVariant.PRIMARY
            )
        }
    }
}

@Composable
fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    onAction: () -> Unit
) {
    AppleCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAction
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) EmeraldLight else SurfaceSubtle),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    tint = if (isGranted) AccentEmerald else InkTitanium,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkTitanium
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = InkSpaceGray,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = BorderHairline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
