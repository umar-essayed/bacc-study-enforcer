package com.baccalaureate.studyenforcer.ui.screens.subjects

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baccalaureate.studyenforcer.data.local.entity.LessonEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
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
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.GoldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    onSolveQuestion: (Long) -> Unit,
    onOpenLesson: (Long) -> Unit = {}
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsState()
    val selectedTopic by viewModel.selectedTopicFilter.collectAsState()
    val availableTopics by viewModel.availableTopics.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val lessons by viewModel.lessons.collectAsState()
    val stats by viewModel.userStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
    ) {
        HeaderBar(
            title = "المناهج والأسئلة",
            subtitle = if (currentTab == 0) "${lessons.size} دروس معتمدة" else "${questions.size} سؤالاً متاحاً"
        )

        // 3 Clean Apple Tabs
        TabRow(
            selectedTabIndex = currentTab,
            containerColor = SurfaceWhite,
            contentColor = InkTitanium,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[currentTab]),
                    color = AccentEmerald,
                    height = 2.5.dp
                )
            }
        ) {
            Tab(
                selected = currentTab == 0,
                onClick = { viewModel.setTab(0) },
                text = {
                    Text(
                        text = "منهج الوزارة والدروس",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentTab == 0) InkTitanium else InkSpaceGray,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
            Tab(
                selected = currentTab == 1,
                onClick = { viewModel.setTab(1) },
                text = {
                    Text(
                        text = "بنك الأسئلة",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentTab == 1) InkTitanium else InkSpaceGray,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
            Tab(
                selected = currentTab == 2,
                onClick = { viewModel.setTab(2) },
                text = {
                    Text(
                        text = "سجل أخطائي",
                        fontSize = 12.sp,
                        fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentTab == 2) AlertCrimson else InkSpaceGray,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            )
        }

        // Subject Filters (Horizontal Pills)
        val subjects = listOf(
            "ARABIC" to "اللغة العربية",
            "ENGLISH" to "English",
            "HISTORY" to "التاريخ",
            "PROGRAMMING" to "البرمجة و AI",
            "ALL" to "الكل"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { (key, label) ->
                val isSelected = selectedSubject == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) InkTitanium else SurfaceWhite)
                        .border(1.dp, if (isSelected) InkTitanium else BorderHairline, RoundedCornerShape(16.dp))
                        .clickable { viewModel.setSubject(key) }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) SurfaceWhite else InkTitanium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Branch / Topic Filters (Only in Question Bank tabs)
        if (currentTab != 0 && availableTopics.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val allTopics = listOf("ALL") + availableTopics
                allTopics.forEach { topic ->
                    val isSelected = selectedTopic == topic
                    val label = if (topic == "ALL") "جميع الفروع" else topic
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentEmerald else SurfaceSubtle)
                            .clickable { viewModel.setTopic(topic) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SurfaceWhite else InkTitanium,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // TAB 0: CURRICULUM LESSONS
        if (currentTab == 0) {
            if (lessons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد دروس مسجلة لهذه المادة حالياً",
                        fontSize = 13.sp,
                        color = InkSpaceGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lessons, key = { it.id }) { lesson ->
                        LessonCardItem(
                            lesson = lesson,
                            isUnlocked = lesson.isUnlocked,
                            onClick = { onOpenLesson(lesson.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // TAB 1 & 2: QUESTION BANK & MISTAKES
            if (questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentTab == 2) "رائع! لا توجد أخطاء مسجلة حالياً 🎉" else "لا توجد أسئلة تطابق الفلاتر المحددة",
                        fontSize = 14.sp,
                        color = InkSpaceGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(questions, key = { it.id }) { question ->
                        QuestionBankCard(
                            question = question,
                            onSolveClick = { onSolveQuestion(question.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCardItem(
    lesson: LessonEntity,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    AppleCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        borderColor = if (lesson.isCompleted) AccentEmerald.copy(alpha = 0.5f) else BorderHairline,
        backgroundColor = if (lesson.isCompleted) EmeraldLight.copy(alpha = 0.15f) else SurfaceWhite
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (lesson.isCompleted) AccentEmerald else if (isUnlocked) SurfaceSubtle else BorderHairline),
                contentAlignment = Alignment.Center
            ) {
                if (lesson.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(20.dp)
                    )
                } else if (!isUnlocked) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = InkSpaceGray,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AutoStories,
                        contentDescription = null,
                        tint = InkTitanium,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lesson.lessonTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) InkTitanium else InkSpaceGray,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${lesson.unitTitle} • ${lesson.lessonType} (${lesson.pageRange})",
                    fontSize = 11.sp,
                    color = InkSpaceGray,
                    maxLines = 1
                )
            }

            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = null,
                    tint = InkSpaceGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun QuestionBankCard(
    question: QuestionEntity,
    onSolveClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val layoutDirection = if (question.languageRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        AppleCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = when {
                question.isAnswered && question.lastAnswerCorrect -> AccentEmerald.copy(alpha = 0.4f)
                question.isAnswered && !question.lastAnswerCorrect -> AlertCrimson.copy(alpha = 0.4f)
                else -> BorderHairline
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Meta row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceSubtle)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (question.lessonTitle.isNotBlank()) "${question.subject} • ${question.lessonTitle}" else "${question.subject} • ${question.topic}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = InkSpaceGray,
                            maxLines = 1
                        )
                    }

                    if (question.isAnswered) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (question.lastAnswerCorrect) EmeraldLight else CrimsonLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (question.lastAnswerCorrect) "صحيحة ✓" else "خطأ سابق ✗",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (question.lastAnswerCorrect) AccentEmerald else AlertCrimson
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldLight)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "لم يُحل بعد",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = AccentGold
                            )
                        }
                    }
                }

                // Question Text
                Text(
                    text = question.questionText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkTitanium,
                    lineHeight = 21.sp,
                    textAlign = if (question.languageRtl) TextAlign.Right else TextAlign.Left
                )

                // If NOT answered: Hide explanation and show "حل السؤال الآن"
                if (!question.isAnswered) {
                    MinimalButton(
                        text = "حل السؤال الآن",
                        onClick = onSolveClick,
                        variant = ButtonStyleVariant.PRIMARY,
                        height = 36.dp
                    )
                } else {
                    // If Answered: allow toggle to review explanation & correct answer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isExpanded = !isExpanded }
                            .background(SurfaceSubtle)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpanded) "إخفاء التفسير والإجابة" else "عرض التفسير والإجابة المعتمدة",
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = InkSpaceGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (question.lastAnswerCorrect) EmeraldLight else CrimsonLight)
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val correctOptionText = when (question.correctOptionIndex) {
                                0 -> question.optionA
                                1 -> question.optionB
                                2 -> question.optionC
                                else -> question.optionD
                            }
                            Text(
                                text = "الإجابة الصحيحة: $correctOptionText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (question.lastAnswerCorrect) AccentEmerald else AlertCrimson
                            )
                            Text(
                                text = question.explanation,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                                color = InkTitanium
                            )
                        }
                    }
                }
            }
        }
    }
}
