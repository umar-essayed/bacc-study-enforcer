package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_hunter_questions")
data class ErrorHunterQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val segmentA: String,
    val segmentB: String,
    val segmentC: String,
    val brokenSegmentIndex: Int, // 0 for A, 1 for B, 2 for C
    val explanation: String,
    val correctedText: String,
    val languageRtl: Boolean = true
)
