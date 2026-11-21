package com.baccalaureate.studyenforcer.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackPressed: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToPolicy: () -> Unit
) {
    val context = LocalContext.current
    val serverIp by viewModel.serverIp.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }

    val fullServerUrl = "http://$serverIp:8888"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeaderBar(
            title = "الإعدادات",
            subtitle = "المزامنة المباشرة وإدارة النظام",
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
            // Status banner if present
            if (statusMessage != null) {
                AppleCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AccentEmerald)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkTitanium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearStatus() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null,
                                tint = InkSpaceGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Card 1: Local Push Server
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Wifi,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "خادم المزامنة المحلي",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkTitanium
                            )
                            Text(
                                text = "مستمع على الشبكة لإرسال الأسئلة والإشعارات",
                                fontSize = 12.sp,
                                color = InkSpaceGray
                            )
                        }

                        IconButton(onClick = { viewModel.refreshIp() }) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = "تحديث العنوان",
                                tint = InkSpaceGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Server endpoint box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceSubtle)
                            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "عنوان الخادم المباشر",
                                    fontSize = 11.sp,
                                    color = InkSpaceGray
                                )
                                Text(
                                    text = fullServerUrl,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = InkTitanium
                                )
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Server URL", fullServerUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ الرابط", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "نسخ",
                                    tint = InkSpaceGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Import buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MinimalButton(
                            text = "استيراد JSON",
                            onClick = { showImportDialog = true },
                            style = ButtonStyleVariant.Secondary,
                            modifier = Modifier.weight(1f)
                        )

                        MinimalButton(
                            text = "لصق الحافظة",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val item = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!item.isNullOrBlank()) {
                                    viewModel.importQuestionsFromJson(item)
                                } else {
                                    Toast.makeText(context, "الحافظة فارغة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            style = ButtonStyleVariant.Primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Card 2: Blocked Apps
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "التطبيقات المحظورة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )
                    Text(
                        text = "يتم منع هذه التطبيقات أثناء فترات التركيز أو العقوبة",
                        fontSize = 12.sp,
                        color = InkSpaceGray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    blockedApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceSubtle)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkTitanium
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 11.sp,
                                    color = InkSpaceGray
                                )
                            }

                            Switch(
                                checked = app.isBlocked,
                                onCheckedChange = { isChecked ->
                                    viewModel.toggleAppBlocked(app.packageName, isChecked)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SurfaceWhite,
                                    checkedTrackColor = AccentEmerald,
                                    uncheckedThumbColor = InkSpaceGray,
                                    uncheckedTrackColor = BorderHairline
                                )
                            )
                        }
                    }
                }
            }

            // Card 3: System Administration & Policy
            AppleCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "إدارة النظام والانضباط",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )

                    MinimalButton(
                        text = "فحص جاهزية الصلاحيات",
                        onClick = onNavigateToPermissions,
                        style = ButtonStyleVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    MinimalButton(
                        text = "سياسة الاستخدام والانضباط",
                        onClick = onNavigateToPolicy,
                        style = ButtonStyleVariant.Secondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    MinimalButton(
                        text = "إلغاء العقوبة وتصفير المخالفات",
                        onClick = { viewModel.resetStrikes() },
                        style = ButtonStyleVariant.Destructive,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // JSON Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text(
                    text = "استيراد أسئلة بتنسيق JSON",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkTitanium
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "الصق مصفوفة JSON للأسئلة أدناه:",
                        fontSize = 12.sp,
                        color = InkSpaceGray
                    )
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentEmerald,
                            unfocusedBorderColor = BorderHairline
                        ),
                        placeholder = {
                            Text(
                                text = "[{\"questionText\": \"...\", \"optionsJson\": \"...\"}]",
                                fontSize = 11.sp,
                                color = InkSpaceGray
                            )
                        }
                    )
                }
            },
            confirmButton = {
                MinimalButton(
                    text = "استيراد",
                    onClick = {
                        if (importJsonText.isNotBlank()) {
                            viewModel.importQuestionsFromJson(importJsonText)
                            importJsonText = ""
                            showImportDialog = false
                        }
                    },
                    style = ButtonStyleVariant.Primary
                )
            },
            dismissButton = {
                MinimalButton(
                    text = "إلغاء",
                    onClick = { showImportDialog = false },
                    style = ButtonStyleVariant.Secondary
                )
            },
            containerColor = SurfaceWhite
        )
    }
}
