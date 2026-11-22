package com.baccalaureate.studyenforcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baccalaureate.studyenforcer.data.local.entity.LessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE subject = :subject ORDER BY id ASC")
    fun getLessonsBySubject(subject: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getLessonById(lessonId: Long): LessonEntity?

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonByIdFlow(lessonId: Long): Flow<LessonEntity?>

    @Query("SELECT * FROM lessons WHERE unitNumber = :unitNumber ORDER BY id ASC")
    fun getLessonsByUnit(unitNumber: Int): Flow<List<LessonEntity>>

    @Query("UPDATE lessons SET isCompleted = :isCompleted, masteryScore = :score WHERE id = :lessonId")
    suspend fun updateLessonProgress(lessonId: Long, isCompleted: Boolean, score: Int)

    @Query("UPDATE lessons SET isUnlocked = 1 WHERE id = :lessonId")
    suspend fun unlockLesson(lessonId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("SELECT COUNT(*) FROM lessons")
    suspend fun getLessonCount(): Int
}
