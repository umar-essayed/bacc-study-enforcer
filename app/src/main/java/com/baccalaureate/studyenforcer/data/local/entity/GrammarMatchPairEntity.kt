package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grammar_match_pairs")
data class GrammarMatchPairEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordOrPhrase: String,
    val grammaticalRule: String,
    val category: String = "ARABIC_NAHW" // ARABIC_NAHW, ENGLISH_TENSES
)
