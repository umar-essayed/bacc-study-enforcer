package com.baccalaureate.studyenforcer.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    onNavigateToPermissions: () -> Unit
) {
    val initialUrl by viewModel.supabaseUrl.collectAsState()
    val initialKey by viewModel.supabaseAnonKey.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()

    var urlInput by remember(initialUrl) { mutableStateOf(initialUrl) }
    var keyInput by remember(initialKey) { mutableStateOf(initialKey) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeaderBar(
            title = "الإعدادات والتكامل السحابي",
            subtitle = "إدارة مزامنة Supabase، قائمة التطبيقات المحظورة، وصلاحيات النظام",
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
            // Permissions Checklist Shortcut
            AppleCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToPermissions
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "فحص جاهزية صلاحيات HyperOS / MIUI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium
                        )
                        Text(
                            text = "الظهور فوق التطبيقات، إمكانية الوصول، وتجاوز توفير الطاقة",
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                    }
                }
            }

            // Supabase Cloud Integration Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudSync,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تكامل بنك الأسئلة مع Supabase",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium
                        )
                    }

                    Text(
                        text = "يمكنك ربط مشروع Supabase الخاص بك لجلب آلاف الأسئلة الإضافية وتحديثها أوتوماتيكياً دون الحاجة لتحديث التطبيق.",
                        fontSize = 12.sp,
                        color = InkSpaceGray,
                        lineHeight = 17.sp
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Project URL (https://xxxx.supabase.co)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald,
                            unfocusedBorderColor = BorderHairline,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Anon / Public API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald,
                            unfocusedBorderColor = BorderHairline,
                            focusedContainerColor = SurfaceWhite,
                            unfocusedContainerColor = SurfaceWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MinimalButton(
                            text = "حفظ الإعدادات",
                            onClick = { viewModel.saveSupabaseConfig(urlInput, keyInput) },
                            modifier = Modifier.weight(1f),
                            variant = ButtonStyleVariant.SECONDARY,
                            height = 42.dp
                        )

                        MinimalButton(
                            text = "اختبار الاتصال",
                            onClick = { viewModel.testConnection() },
                            modifier = Modifier.weight(1f),
                            variant = ButtonStyleVariant.SECONDARY,
                            height = 42.dp
                        )
                    }

                    MinimalButton(
                        text = "⚡ مزامنة بنك الأسئلة الآن (1-Tap Sync)",
                        onClick = { viewModel.syncFromSupabase() },
                        enabled = !isSyncing,
                        variant = ButtonStyleVariant.PRIMARY,
                        height = 46.dp
                    )

                    if (isSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AccentEmerald,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "جاري الاتصال...", fontSize = 12.sp, color = InkSpaceGray)
                        }
                    }

                    if (syncStatus != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = syncStatus!!,
                                fontSize = 12.sp,
                                color = InkTitanium
                            )
                        }
                    }
                }
            }

            // Blocked Apps Management Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "التطبيقات المشمولة بالحظر أثناء القفل والعقوبة:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )

                    blockedApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkTitanium
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 10.sp,
                                    color = InkSpaceGray
                                )
                            }
                            Switch(
                                checked = app.isBlocked,
                                onCheckedChange = { checked ->
                                    viewModel.toggleAppBlocked(app.packageName, checked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SurfaceWhite,
                                    checkedTrackColor = AccentEmerald,
                                    uncheckedThumbColor = SurfaceWhite,
                                    uncheckedTrackColor = BorderHairline
                                )
                            )
                        }
                    }
                }
            }

            // Reset Strikes Card
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "إعادة ضبط عداد الضربات والمخالفات:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )
                    MinimalButton(
                        text = "تصفير عداد الضربات يدوياً (Reset Strikes)",
                        onClick = { viewModel.resetStrikes() },
                        variant = ButtonStyleVariant.DESTRUCTIVE,
                        height = 42.dp
                    )
                }
            }
        }
    }
}
