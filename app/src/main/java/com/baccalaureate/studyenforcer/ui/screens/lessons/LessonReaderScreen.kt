package com.baccalaureate.studyenforcer.ui.screens.lessons

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.ButtonStyleVariant
import com.baccalaureate.studyenforcer.ui.components.MinimalButton
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun LessonReaderScreen(
    viewModel: LessonReaderViewModel,
    onBack: () -> Unit,
    onStartLessonQuiz: (Long) -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val questions by viewModel.questions.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        if (lesson == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CanvasAlabaster),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentEmerald)
            }
        } else {
            val l = lesson!!
            val gson = Gson()
            val listType = object : TypeToken<List<String>>() {}.type

            val conceptFlow: List<String> = try {
                gson.fromJson(l.conceptFlowJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val rhetoricKeys: List<String> = try {
                gson.fromJson(l.rhetoricKeysJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val grammarRules: List<String> = try {
                gson.fromJson(l.grammarRulesJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CanvasAlabaster)
            ) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "رجوع",
                            tint = InkTitanium
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = l.lessonTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkTitanium,
                            maxLines = 1
                        )
                        Text(
                            text = "${l.unitTitle} • ${l.lessonType} (${l.pageRange})",
                            fontSize = 11.sp,
                            color = InkSpaceGray,
                            maxLines = 1
                        )
                    }
                    if (l.isCompleted) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "مكتمل ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Metadata & Central Thesis Card
                    AppleCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FormatQuote,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "بطاقة تعريف الدرس والرؤية الحاكمة",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkTitanium
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InfoTag(label = "الكاتب/الشاعر", value = l.author)
                                InfoTag(label = "النوع", value = l.lessonType)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GoldLight)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "السؤال المحوري:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentGold
                                    )
                                    Text(
                                        text = l.coreQuestion,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = InkTitanium,
                                        lineHeight = 19.sp
                                    )
                                }
                            }

                            Text(
                                text = l.coreThesis,
                                fontSize = 13.sp,
                                color = InkSpaceGray,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // 2. Concept Flow & Logic Hierarchy
                    if (conceptFlow.isNotEmpty()) {
                        AppleCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoStories,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "مسار الأفكار والتحليل الهيكلي",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkTitanium
                                    )
                                }

                                conceptFlow.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceSubtle)
                                                .border(1.dp, BorderHairline, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkTitanium
                                            )
                                        }
                                        Text(
                                            text = step,
                                            fontSize = 13.sp,
                                            color = InkTitanium,
                                            lineHeight = 20.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Rhetoric & Literary Keys
                    if (rhetoricKeys.isNotEmpty()) {
                        AppleCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "مفاتيح البلاغة والتذوق الجمالي",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkTitanium
                                    )
                                }

                                rhetoricKeys.forEach { key ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AccentGold)
                                                .padding(top = 7.dp)
                                        )
                                        Text(
                                            text = key,
                                            fontSize = 13.sp,
                                            color = InkTitanium,
                                            lineHeight = 19.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Grammar & Orthography Workshop
                    if (grammarRules.isNotEmpty()) {
                        AppleCard(modifier = Modifier.fillMaxWidth()) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.MenuBook,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "المحطة النحوية والإملائية المستهدفة",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkTitanium
                                    )
                                }

                                grammarRules.forEach { rule ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(SurfaceSubtle)
                                            .border(1.dp, BorderHairline, RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = rule,
                                            fontSize = 12.sp,
                                            color = InkTitanium,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Practice & Mastery Quiz Call-To-Action
                    AppleCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = AccentEmerald.copy(alpha = 0.5f),
                        backgroundColor = EmeraldLight.copy(alpha = 0.4f)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Quiz,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "تدريبات نواتج التعلم الوزارية",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkTitanium
                            )
                            Text(
                                text = "تم إعداد ${questions.size} أسئلة MCQ دقيقة ومباشرة من صلب تدريبات الوزارة لقياس إتقانك لهذا الدرس.",
                                fontSize = 12.sp,
                                color = InkSpaceGray,
                                lineHeight = 18.sp
                            )
                            MinimalButton(
                                text = if (l.isCompleted) "إعادة اختبار الدرس (${questions.size} أسئلة)" else "بدء اختبار إتقان الدرس",
                                onClick = { onStartLessonQuiz(l.id) },
                                variant = ButtonStyleVariant.PRIMARY,
                                height = 44.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun InfoTag(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceSubtle)
            .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = InkSpaceGray
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = InkTitanium
            )
        }
    }
}
