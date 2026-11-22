package com.baccalaureate.studyenforcer.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val titleAr: String, val titleEn: String, val icon: ImageVector) {
    object Today : Screen("today", "اليوم", "Today", Icons.Outlined.Today)
    object Subjects : Screen("subjects", "المواد", "Subjects", Icons.Outlined.AutoStories)
    object FocusLocker : Screen("locker", "القفل", "Locker", Icons.Outlined.LockClock)
    object Games : Screen("games", "التدريب", "Games", Icons.Outlined.Bolt)

    // Sub-screens & Workflows
    object Settings : Screen("settings", "الإعدادات", "Settings", Icons.Outlined.Today)
    object SpeedGrammar : Screen("speed_grammar", "طابق الإعراب", "Grammar Match", Icons.Outlined.Bolt)
    object ErrorHunter : Screen("error_hunter", "صائد الأخطاء", "Error Hunter", Icons.Outlined.Bolt)
    object Permissions : Screen("permissions", "الصلاحيات", "Permissions", Icons.Outlined.LockClock)
    object Onboarding : Screen("onboarding", "البداية", "Onboarding", Icons.Outlined.Today)
    object Policy : Screen("policy", "سياسة الانضباط", "Policy", Icons.Outlined.LockClock)
    object ActiveQuiz : Screen("quiz/{questionId}", "الاختبار", "Quiz", Icons.Outlined.Today) {
        fun createRoute(questionId: Long = -1L): String = "quiz/$questionId"
    }
    object LessonReader : Screen("lesson_reader/{lessonId}", "الدرس", "Lesson", Icons.Outlined.AutoStories) {
        fun createRoute(lessonId: Long): String = "lesson_reader/$lessonId"
    }
    object TrueFalse : Screen("true_false?subject={subject}&topic={topic}", "صح أم خطأ", "True / False", Icons.Outlined.Bolt) {
        fun createRoute(subject: String = "ALL", topic: String? = null): String {
            return if (topic != null) "true_false?subject=$subject&topic=$topic" else "true_false?subject=$subject"
        }
    }
    object SyntaxFill : Screen("syntax_fill?subject={subject}&topic={topic}", "أكمل الفراغ", "Syntax Fill", Icons.Outlined.Bolt) {
        fun createRoute(subject: String = "ALL", topic: String? = null): String {
            return if (topic != null) "syntax_fill?subject=$subject&topic=$topic" else "syntax_fill?subject=$subject"
        }
    }
}

val BottomNavItems = listOf(
    Screen.Today,
    Screen.Subjects,
    Screen.FocusLocker,
    Screen.Games
)
