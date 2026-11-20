package com.baccalaureate.studyenforcer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LockerStateDao {

    @Query("SELECT * FROM locker_state WHERE id = 1 LIMIT 1")
    fun getLockerStateFlow(): Flow<LockerStateEntity?>

    @Query("SELECT * FROM locker_state WHERE id = 1 LIMIT 1")
    suspend fun getLockerState(): LockerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLockerState(state: LockerStateEntity)

    @Query("UPDATE locker_state SET isLocked = 0, lockEndTimeEpoch = 0, isPenaltyMode = 0 WHERE id = 1")
    suspend fun clearLock()
}
