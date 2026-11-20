package com.baccalaureate.studyenforcer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locker_state")
data class LockerStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val isLocked: Boolean = false,
    val lockEndTimeEpoch: Long = 0L,
    val originalDurationMinutes: Int = 0,
    val isPenaltyMode: Boolean = false,
    val penaltyReason: String = ""
) {
    val remainingSeconds: Long
        get() {
            if (!isLocked) return 0L
            val diff = (lockEndTimeEpoch - System.currentTimeMillis()) / 1000
            return if (diff > 0) diff else 0L
        }

    val isActive: Boolean
        get() = isLocked && remainingSeconds > 0
}
