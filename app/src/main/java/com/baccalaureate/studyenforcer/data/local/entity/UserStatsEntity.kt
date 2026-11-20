package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey
    val id: Int = 1,
    val streakDays: Int = 3,
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val strikesCount: Int = 0, // 0..3 (Triggers penalty at 3)
    val snoozeTokensRemaining: Int = 2, // Max 2 per day
    val lastSnoozeResetDate: String = "",
    val totalFocusMinutes: Int = 45,
    val lastActiveDate: String = ""
)
