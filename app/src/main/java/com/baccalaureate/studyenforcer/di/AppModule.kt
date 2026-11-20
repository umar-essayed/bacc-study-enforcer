package com.baccalaureate.studyenforcer.di

import android.content.Context
import androidx.room.Room
import com.baccalaureate.studyenforcer.data.local.AppDatabase
import com.baccalaureate.studyenforcer.data.local.dao.BlockedAppDao
import com.baccalaureate.studyenforcer.data.local.dao.GamesDao
import com.baccalaureate.studyenforcer.data.local.dao.LockerStateDao
import com.baccalaureate.studyenforcer.data.local.dao.QuestionDao
import com.baccalaureate.studyenforcer.data.local.dao.UserStatsDao
import com.baccalaureate.studyenforcer.data.remote.SupabaseApi
import com.baccalaureate.studyenforcer.data.remote.SupabaseConfig
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bacc_study_enforcer.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideUserStatsDao(db: AppDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    fun provideBlockedAppDao(db: AppDatabase): BlockedAppDao = db.blockedAppDao()

    @Provides
    fun provideLockerStateDao(db: AppDatabase): LockerStateDao = db.lockerStateDao()

    @Provides
    fun provideGamesDao(db: AppDatabase): GamesDao = db.gamesDao()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSupabaseConfig(@ApplicationContext context: Context): SupabaseConfig {
        return SupabaseConfig(context)
    }

    @Provides
    @Singleton
    fun provideSupabaseApi(
        client: OkHttpClient,
        gson: Gson,
        config: SupabaseConfig
    ): SupabaseApi {
        return SupabaseApi(client, gson, config)
    }

    @Provides
    @Singleton
    fun provideStudyRepository(
        @ApplicationContext context: Context,
        db: AppDatabase,
        supabaseApi: SupabaseApi,
        gson: Gson
    ): StudyRepository {
        return StudyRepository(context, db, supabaseApi, gson)
    }
}
