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

    // Sub-screens
    object Settings : Screen("settings", "الإعدادات", "Settings", Icons.Outlined.Today)
    object SpeedGrammar : Screen("speed_grammar", "طابق الإعراب", "Grammar Match", Icons.Outlined.Bolt)
    object ErrorHunter : Screen("error_hunter", "صائد الأخطاء", "Error Hunter", Icons.Outlined.Bolt)
    object Permissions : Screen("permissions", "الصلاحيات", "Permissions", Icons.Outlined.LockClock)
}

val BottomNavItems = listOf(
    Screen.Today,
    Screen.Subjects,
    Screen.FocusLocker,
    Screen.Games
)
