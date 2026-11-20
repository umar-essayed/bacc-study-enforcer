package com.baccalaureate.studyenforcer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.baccalaureate.studyenforcer.data.local.dao.BlockedAppDao
import com.baccalaureate.studyenforcer.data.local.dao.GamesDao
import com.baccalaureate.studyenforcer.data.local.dao.LockerStateDao
import com.baccalaureate.studyenforcer.data.local.dao.QuestionDao
import com.baccalaureate.studyenforcer.data.local.dao.UserStatsDao
import com.baccalaureate.studyenforcer.data.local.entity.BlockedAppEntity
import com.baccalaureate.studyenforcer.data.local.entity.ErrorHunterQuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.GrammarMatchPairEntity
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.SubjectProgressEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity

@Database(
    entities = [
        QuestionEntity::class,
        UserStatsEntity::class,
        SubjectProgressEntity::class,
        BlockedAppEntity::class,
        LockerStateEntity::class,
        GrammarMatchPairEntity::class,
        ErrorHunterQuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun blockedAppDao(): BlockedAppDao
    abstract fun lockerStateDao(): LockerStateDao
    abstract fun gamesDao(): GamesDao
}
