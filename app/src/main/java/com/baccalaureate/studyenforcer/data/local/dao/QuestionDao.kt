package com.baccalaureate.studyenforcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY id ASC")
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject AND topic = :topic ORDER BY id ASC")
    fun getQuestionsBySubjectAndTopic(subject: String, topic: String): Flow<List<QuestionEntity>>

    @Query("SELECT DISTINCT topic FROM questions WHERE subject = :subject")
    fun getTopicsForSubject(subject: String): Flow<List<String>>

    @Query("SELECT * FROM questions WHERE isAnswered = 1 AND lastAnswerCorrect = 0 ORDER BY id DESC")
    fun getMistakeQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestion(): QuestionEntity?

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestionBySubject(subject: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE subject = :subject AND topic = :topic ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestionBySubjectAndTopic(subject: String, topic: String): QuestionEntity?

    @Query("UPDATE questions SET isAnswered = 1, lastAnswerCorrect = :isCorrect WHERE id = :id")
    suspend fun markQuestionAnswered(id: Long, isCorrect: Boolean)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Query("DELETE FROM questions")
    suspend fun clearAll()
}
