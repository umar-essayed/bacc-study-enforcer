package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: Long, // 101, 102, 103, 201, 202, 203, 301, 302, 303
    @SerializedName("subject")
    val subject: String = "ARABIC",
    @SerializedName("unitNumber")
    val unitNumber: Int = 1,
    @SerializedName("unitTitle")
    val unitTitle: String = "",
    @SerializedName("lessonNumber")
    val lessonNumber: Int = 1,
    @SerializedName("lessonTitle")
    val lessonTitle: String = "",
    @SerializedName("lessonType")
    val lessonType: String = "", // نص معلوماتي, نص نثري, نص شعري
    @SerializedName("author")
    val author: String = "",
    @SerializedName("pageRange")
    val pageRange: String = "",
    @SerializedName("analyticalLens")
    val analyticalLens: String = "",
    @SerializedName("coreQuestion")
    val coreQuestion: String = "",
    @SerializedName("coreThesis")
    val coreThesis: String = "",
    @SerializedName("conceptFlowJson")
    val conceptFlowJson: String = "[]",
    @SerializedName("rhetoricKeysJson")
    val rhetoricKeysJson: String = "[]",
    @SerializedName("grammarRulesJson")
    val grammarRulesJson: String = "[]",
    val isUnlocked: Boolean = true,
    val isCompleted: Boolean = false,
    val masteryScore: Int = 0
)
