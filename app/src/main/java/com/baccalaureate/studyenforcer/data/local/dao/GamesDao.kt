package com.baccalaureate.studyenforcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baccalaureate.studyenforcer.data.local.entity.ErrorHunterQuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.GrammarMatchPairEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GamesDao {

    @Query("SELECT * FROM grammar_match_pairs ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomGrammarPairs(limit: Int = 5): List<GrammarMatchPairEntity>

    @Query("SELECT * FROM grammar_match_pairs WHERE category = :category ORDER BY RANDOM() LIMIT :limit")
    suspend fun getGrammarPairsByCategory(category: String, limit: Int = 5): List<GrammarMatchPairEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarPairs(pairs: List<GrammarMatchPairEntity>)

    @Query("SELECT COUNT(*) FROM grammar_match_pairs")
    suspend fun getGrammarPairCount(): Int

    @Query("SELECT * FROM error_hunter_questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomErrorHunterQuestions(limit: Int = 10): List<ErrorHunterQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorHunterQuestions(questions: List<ErrorHunterQuestionEntity>)

    @Query("SELECT COUNT(*) FROM error_hunter_questions")
    suspend fun getErrorHunterCount(): Int
}
