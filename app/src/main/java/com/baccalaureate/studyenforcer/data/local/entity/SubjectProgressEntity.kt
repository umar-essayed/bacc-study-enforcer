package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subject_progress")
data class SubjectProgressEntity(
    @PrimaryKey
    val subject: String, // ARABIC, ENGLISH, HISTORY, PROGRAMMING
    val totalQuestions: Int = 0,
    val answeredCount: Int = 0,
    val correctCount: Int = 0
) {
    val accuracy: Float
        get() = if (answeredCount > 0) (correctCount.toFloat() / answeredCount.toFloat()) * 100f else 0f
}
