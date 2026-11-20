package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,
    @SerializedName("subject")
    val subject: String, // ARABIC, ENGLISH, HISTORY, PROGRAMMING
    @SerializedName("topic")
    val topic: String,
    @SerializedName("questionText")
    val questionText: String,
    @SerializedName("optionA")
    val optionA: String,
    @SerializedName("optionB")
    val optionB: String,
    @SerializedName("optionC")
    val optionC: String,
    @SerializedName("optionD")
    val optionD: String,
    @SerializedName("correctOptionIndex")
    val correctOptionIndex: Int, // 0..3
    @SerializedName("explanation")
    val explanation: String,
    @SerializedName("languageRtl")
    val languageRtl: Boolean = true,
    @SerializedName("difficulty")
    val difficulty: String = "MEDIUM" // EASY, MEDIUM, HARD
)
