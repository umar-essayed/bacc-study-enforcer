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
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Icon
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
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.ui.components.AppleCard
import com.baccalaureate.studyenforcer.ui.components.HeaderBar
import com.baccalaureate.studyenforcer.ui.theme.AccentEmerald
import com.baccalaureate.studyenforcer.ui.theme.BorderHairline
import com.baccalaureate.studyenforcer.ui.theme.CanvasAlabaster
import com.baccalaureate.studyenforcer.ui.theme.EmeraldLight
import com.baccalaureate.studyenforcer.ui.theme.InkSpaceGray
import com.baccalaureate.studyenforcer.ui.theme.InkTitanium
import com.baccalaureate.studyenforcer.ui.theme.SurfaceSubtle
import com.baccalaureate.studyenforcer.ui.theme.SurfaceWhite

@Composable
fun SubjectsScreen(viewModel: SubjectsViewModel) {
    val selectedFilter by viewModel.selectedSubjectFilter.collectAsState()
    val questions by viewModel.questions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasAlabaster)
    ) {
        HeaderBar(
            title = "بنك الأسئلة المعتمد",
            subtitle = "${questions.size} سؤالاً تأسيسياً للمسار العلمي والذكاء الاصطناعي"
        )

        // Filter Pills
        val filters = listOf(
            "ALL" to "الكل",
            "ARABIC" to "النحو العربي",
            "ENGLISH" to "English",
            "HISTORY" to "تاريخ مصر",
            "PROGRAMMING" to "البرمجة و AI"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (key, label) ->
                val isSelected = selectedFilter == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) InkTitanium else SurfaceWhite)
                        .border(1.dp, if (isSelected) InkTitanium else BorderHairline, RoundedCornerShape(20.dp))
                        .clickable { viewModel.setFilter(key) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) SurfaceWhite else InkTitanium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Questions List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(questions, key = { it.id }) { question ->
                QuestionBrowserCard(question = question)
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun QuestionBrowserCard(question: QuestionEntity) {
    var expanded by remember { mutableStateOf(false) }
    val direction = if (question.languageRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        AppleCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { expanded = !expanded }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Topic & Difficulty Badge
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
                            text = question.topic,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkTitanium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (question.difficulty) {
                                "EASY" -> "مباشر"
                                "HARD" -> "متقدم"
                                else -> "متوسط"
                            },
                            fontSize = 11.sp,
                            color = InkSpaceGray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            tint = InkSpaceGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Question Prompt
                Text(
                    text = question.questionText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkTitanium,
                    lineHeight = 22.sp,
                    textAlign = if (question.languageRtl) TextAlign.Right else TextAlign.Left
                )

                // Expandable Options & Explanation
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val options = listOf(
                            0 to question.optionA,
                            1 to question.optionB,
                            2 to question.optionC,
                            3 to question.optionD
                        )

                        options.forEach { (idx, optText) ->
                            val isCorrect = idx == question.correctOptionIndex
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCorrect) EmeraldLight else SurfaceSubtle)
                                    .border(1.dp, if (isCorrect) AccentEmerald.copy(alpha = 0.5f) else BorderHairline, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AccentEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = optText,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCorrect) AccentEmerald else InkTitanium
                                )
                            }
                        }

                        // Explanation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceWhite)
                                .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "الإيضاح: ${question.explanation}",
                                fontSize = 12.sp,
                                color = InkSpaceGray,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
