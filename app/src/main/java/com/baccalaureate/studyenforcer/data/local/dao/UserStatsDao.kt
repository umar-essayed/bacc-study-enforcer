package com.baccalaureate.studyenforcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.baccalaureate.studyenforcer.data.local.entity.SubjectProgressEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET userName = :name, isOnboardingCompleted = 1 WHERE id = 1")
    suspend fun completeOnboarding(name: String)

    @Query("UPDATE user_stats SET strikesCount = :strikes WHERE id = 1")
    suspend fun updateStrikes(strikes: Int)

    @Query("UPDATE user_stats SET snoozeTokensRemaining = :tokens WHERE id = 1")
    suspend fun updateSnoozeTokens(tokens: Int)

    @Query("UPDATE user_stats SET totalFocusMinutes = totalFocusMinutes + :additionalMinutes WHERE id = 1")
    suspend fun addFocusMinutes(additionalMinutes: Int)

    @Query("SELECT * FROM subject_progress")
    fun getAllSubjectProgress(): Flow<List<SubjectProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjectProgress(progress: SubjectProgressEntity)

    @Query("SELECT * FROM subject_progress WHERE subject = :subject LIMIT 1")
    suspend fun getSubjectProgress(subject: String): SubjectProgressEntity?

    @Transaction
    suspend fun recordAnswer(subject: String, isCorrect: Boolean) {
        val currentStats = getUserStats() ?: UserStatsEntity()
        val newTotal = currentStats.totalAnswered + 1
        val newCorrect = if (isCorrect) currentStats.correctCount + 1 else currentStats.correctCount
        val newStrikes = if (isCorrect) {
            0
        } else {
            currentStats.strikesCount + 1
        }
        insertOrUpdateStats(
            currentStats.copy(
                totalAnswered = newTotal,
                correctCount = newCorrect,
                strikesCount = newStrikes
            )
        )

        val progress = getSubjectProgress(subject) ?: SubjectProgressEntity(subject = subject)
        insertSubjectProgress(
            progress.copy(
                answeredCount = progress.answeredCount + 1,
                correctCount = if (isCorrect) progress.correctCount + 1 else progress.correctCount
            )
        )
    }
}
