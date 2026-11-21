package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
import com.baccalaureate.studyenforcer.ui.theme.AccentCrimson
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.AccentGold
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun GamesScreen(
    viewModel: GamesViewModel,
    onNavigateToSpeedGrammar: () -> Unit,
    onNavigateToErrorHunter: () -> Unit,
    onNavigateToTrueFalse: (subject: String, topic: String?) -> Unit,
    onNavigateToSyntaxFill: (subject: String, topic: String?) -> Unit
) {
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val availableTopics by viewModel.availableTopics.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        HeaderBar(
            title = "ألعاب وتدريبات",
            subtitle = "تثبيت المنهج عبر تدريبات وتحديات سريعة"
        )

        // Subject selector pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.subjects.forEach { subject ->
                val isSelected = selectedSubject == subject.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) InkTitanium else SurfaceWhite)
                        .border(1.dp, if (isSelected) InkTitanium else BorderHairline, RoundedCornerShape(20.dp))
                        .clickable { viewModel.selectSubject(subject.id) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = subject.title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SurfaceWhite else InkSpaceGray
                    )
                }
            }
        }

        // Branch / Topic selector pills (if subject selected and topics available)
        if (availableTopics.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "كل الفروع"
                val isAllTopics = selectedTopic == null
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isAllTopics) EmeraldLight else SurfaceSubtle)
                        .border(1.dp, if (isAllTopics) AccentEmerald else BorderHairline, RoundedCornerShape(16.dp))
                        .clickable { viewModel.selectTopic(null) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "كافة الفروع",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isAllTopics) AccentEmerald else InkSpaceGray
                    )
                }

                availableTopics.forEach { topic ->
                    val isSelected = selectedTopic == topic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) EmeraldLight else SurfaceSubtle)
                            .border(1.dp, if (isSelected) AccentEmerald else BorderHairline, RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectTopic(topic) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = topic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) AccentEmerald else InkSpaceGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Games Grid / Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game 1: True / False Sprint
            GameCard(
                icon = Icons.Outlined.CheckCircle,
                iconTint = AccentEmerald,
                iconBg = EmeraldLight,
                title = "سباق الصح والخطأ الخاطف",
                subtitle = "30 ثانية لتحديد صحة أو خطأ العبارات بأعلى دقة وسرعة",
                badgeText = "تحدي 30 ثانية",
                buttonText = "بدء السباق الخاطف",
                onClick = { onNavigateToTrueFalse(selectedSubject, selectedTopic) }
            )

            // Game 2: Syntax Fill
            GameCard(
                icon = Icons.Outlined.EditNote,
                iconTint = AccentGold,
                iconBg = GoldLight,
                title = "أكمل الفراغ النحوي والعلمي",
                subtitle = "تدريب مركز لاختيار الكلمة أو القاعدة الصحيحة من 4 خيارات",
                badgeText = "تدريب تركيز",
                buttonText = "بدء تدريب الفراغات",
                onClick = { onNavigateToSyntaxFill(selectedSubject, selectedTopic) }
            )

            // Game 3: Speed Grammar Match
            GameCard(
                icon = Icons.Outlined.Bolt,
                iconTint = AccentEmerald,
                iconBg = EmeraldLight,
                title = "طابق الإعراب (Speed Match)",
                subtitle = "طابق الكلمات والجمل بالحكم الإعرابي أو القاعدة المناسبة قبل نفاد الوقت",
                badgeText = "تحدي 60 ثانية",
                buttonText = "بدء مطابقة الإعراب",
                onClick = onNavigateToSpeedGrammar
            )

            // Game 4: Error Hunter
            GameCard(
                icon = Icons.Outlined.Search,
                iconTint = AccentGold,
                iconBg = GoldLight,
                title = "صائد الأخطاء النحوية",
                subtitle = "اكتشف الكلمة الشاذة أو الخطأ الإعرابي في الجملة قبل انتهاء 10 ثوانٍ",
                badgeText = "صيد خاطف 10 ثوانٍ",
                buttonText = "بدء صيد الأخطاء",
                onClick = onNavigateToErrorHunter
            )
        }
    }
}

@Composable
private fun GameCard(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconBg: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    badgeText: String,
    buttonText: String,
    onClick: () -> Unit
) {
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkTitanium
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = InkSpaceGray,
                        lineHeight = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkSpaceGray
                    )
                }

                MinimalButton(
                    text = buttonText,
                    onClick = onClick,
                    style = ButtonStyleVariant.Primary
                )
            }
        }
    }
}
