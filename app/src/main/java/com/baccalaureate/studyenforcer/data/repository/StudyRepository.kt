package com.baccalaureate.studyenforcer.data.repository

import android.content.Context
import com.baccalaureate.studyenforcer.data.local.AppDatabase
import com.baccalaureate.studyenforcer.data.local.entity.BlockedAppEntity
import com.baccalaureate.studyenforcer.data.local.entity.ErrorHunterQuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.GrammarMatchPairEntity
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.SubjectProgressEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity
import com.baccalaureate.studyenforcer.data.remote.SupabaseApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val supabaseApi: SupabaseApi,
    private val gson: Gson
) {
    private val questionDao = db.questionDao()
    private val userStatsDao = db.userStatsDao()
    private val blockedAppDao = db.blockedAppDao()
    private val lockerStateDao = db.lockerStateDao()
    private val gamesDao = db.gamesDao()

    fun getAllQuestions(): Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsBySubject(subject)

    suspend fun getRandomQuestion(): QuestionEntity? = questionDao.getRandomQuestion()
    suspend fun getRandomQuestionBySubject(subject: String): QuestionEntity? =
        questionDao.getRandomQuestionBySubject(subject)

    fun getUserStatsFlow(): Flow<UserStatsEntity?> = userStatsDao.getUserStatsFlow()
    fun getAllSubjectProgress(): Flow<List<SubjectProgressEntity>> =
        userStatsDao.getAllSubjectProgress()

    fun getLockerStateFlow(): Flow<LockerStateEntity?> = lockerStateDao.getLockerStateFlow()
    suspend fun getLockerState(): LockerStateEntity? = lockerStateDao.getLockerState()

    fun getBlockedAppsFlow(): Flow<List<BlockedAppEntity>> = blockedAppDao.getAllBlockedApps()
    suspend fun isAppBlocked(packageName: String): Boolean = blockedAppDao.isAppBlocked(packageName)

    suspend fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        blockedAppDao.updateAppBlockedStatus(packageName, isBlocked)
    }

    suspend fun addCustomBlockedApp(packageName: String, appName: String) {
        blockedAppDao.insertApp(BlockedAppEntity(packageName = packageName, appName = appName, isBlocked = true))
    }

    /**
     * Records user answer and enforces 3-strikes rule.
     * If 3 consecutive wrong answers occur, Penalty Mode is activated for 60 minutes.
     */
    suspend fun recordAnswer(question: QuestionEntity, selectedOptionIndex: Int): AnswerResult {
        val isCorrect = question.correctOptionIndex == selectedOptionIndex
        userStatsDao.recordAnswer(question.subject, isCorrect)

        val updatedStats = userStatsDao.getUserStats() ?: UserStatsEntity()
        if (updatedStats.strikesCount >= 3) {
            // Activate Penalty Mode for 60 minutes
            activatePenaltyMode(60, "تجاوز 3 إجابات خاطئة متتالية في الاختبار")
            return AnswerResult(
                isCorrect = false,
                strikesCount = updatedStats.strikesCount,
                penaltyActivated = true,
                explanation = question.explanation
            )
        }

        return AnswerResult(
            isCorrect = isCorrect,
            strikesCount = updatedStats.strikesCount,
            penaltyActivated = false,
            explanation = question.explanation
        )
    }

    /**
     * Activates strict study locker for a designated duration (minutes).
     */
    suspend fun activateLocker(durationMinutes: Int) {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        lockerStateDao.setLockerState(
            LockerStateEntity(
                id = 1,
                isLocked = true,
                lockEndTimeEpoch = endTime,
                originalDurationMinutes = durationMinutes,
                isPenaltyMode = false,
                penaltyReason = ""
            )
        )
        userStatsDao.addFocusMinutes(durationMinutes)
    }

    /**
     * Activates 3-Strikes Penalty Mode.
     */
    suspend fun activatePenaltyMode(durationMinutes: Int = 60, reason: String = "عقوبة 3 ضربات متتالية") {
        val endTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        lockerStateDao.setLockerState(
            LockerStateEntity(
                id = 1,
                isLocked = true,
                lockEndTimeEpoch = endTime,
                originalDurationMinutes = durationMinutes,
                isPenaltyMode = true,
                penaltyReason = reason
            )
        )
    }

    suspend fun unlockPenaltyEarlyAfterReview() {
        // Resets strikes and lifts lock
        userStatsDao.updateStrikes(0)
        lockerStateDao.clearLock()
    }

    /**
     * Attempts to consume 1 emergency snooze token (max 2 per day).
     * Returns true if granted, false if exhausted.
     */
    suspend fun consumeSnoozeToken(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        var stats = userStatsDao.getUserStats() ?: UserStatsEntity()

        // Daily reset check
        if (stats.lastSnoozeResetDate != today) {
            stats = stats.copy(snoozeTokensRemaining = 2, lastSnoozeResetDate = today)
            userStatsDao.insertOrUpdateStats(stats)
        }

        if (stats.snoozeTokensRemaining > 0) {
            userStatsDao.updateSnoozeTokens(stats.snoozeTokensRemaining - 1)
            return true
        }
        return false
    }

    /**
     * Syncs remote questions from Supabase PostgREST table into Room.
     */
    suspend fun syncWithSupabase(): Result<Int> {
        return supabaseApi.fetchRemoteQuestions().mapCatching { remoteQuestions ->
            if (remoteQuestions.isNotEmpty()) {
                questionDao.insertQuestions(remoteQuestions)
            }
            remoteQuestions.size
        }
    }

    // --- Games API ---
    suspend fun getGrammarMatchPairs(limit: Int = 6): List<GrammarMatchPairEntity> =
        gamesDao.getRandomGrammarPairs(limit)

    suspend fun getErrorHunterQuestions(limit: Int = 10): List<ErrorHunterQuestionEntity> =
        gamesDao.getRandomErrorHunterQuestions(limit)

    /**
     * Seeds initial database from assets and defaults if empty.
     */
    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Seed Questions from seed_questions.json
        if (questionDao.getQuestionCount() == 0) {
            try {
                context.assets.open("seed_questions.json").use { stream ->
                    val reader = InputStreamReader(stream)
                    val type = object : TypeToken<List<QuestionEntity>>() {}.type
                    val questions: List<QuestionEntity> = gson.fromJson(reader, type)
                    questionDao.insertQuestions(questions)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Initialize User Stats & Subject Progress
        if (userStatsDao.getUserStats() == null) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            userStatsDao.insertOrUpdateStats(
                UserStatsEntity(
                    id = 1,
                    streakDays = 5,
                    totalAnswered = 18,
                    correctCount = 16,
                    strikesCount = 0,
                    snoozeTokensRemaining = 2,
                    lastSnoozeResetDate = today,
                    totalFocusMinutes = 90,
                    lastActiveDate = today
                )
            )

            // Seed Subject Progress
            listOf("ARABIC", "ENGLISH", "HISTORY", "PROGRAMMING").forEach { sub ->
                userStatsDao.insertSubjectProgress(
                    SubjectProgressEntity(
                        subject = sub,
                        totalQuestions = 15,
                        answeredCount = 4,
                        correctCount = 4
                    )
                )
            }
        }

        // 3. Initialize Default Blocked Apps (Social media / Distractions)
        if (blockedAppDao.getAppCount() == 0) {
            val defaultBlocked = listOf(
                BlockedAppEntity("com.instagram.android", "Instagram", true),
                BlockedAppEntity("com.facebook.katana", "Facebook", true),
                BlockedAppEntity("com.zhiliaoapp.musically", "TikTok", true),
                BlockedAppEntity("com.google.android.youtube", "YouTube", true),
                BlockedAppEntity("com.twitter.android", "X (Twitter)", true),
                BlockedAppEntity("com.reddit.frontpage", "Reddit", true),
                BlockedAppEntity("com.snapchat.android", "Snapchat", true),
                BlockedAppEntity("com.facebook.orca", "Messenger", false)
            )
            blockedAppDao.insertApps(defaultBlocked)
        }

        // 4. Seed Grammar Match Pairs if empty
        if (gamesDao.getGrammarPairCount() == 0) {
            val defaultPairs = listOf(
                GrammarMatchPairEntity(wordOrPhrase = "أبو بكرٍ الصديق", grammaticalRule = "مبتدأ مرفوع بالواو (أسماء خمسة)", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "لم يضيعوا وقتهم", grammaticalRule = "مضارع مجزوم بحذف النون (أفعال خمسة)", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "لعلّ النصرَ قريبٌ", grammaticalRule = "اسم لعل منصوب بالفتحة", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "لا تظلمْ فتندمَ", grammaticalRule = "مضارع منصوب بفاء السببية", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "ما كان المؤمنُ ليكذبَ", grammaticalRule = "مضارع منصوب بلام الجحود", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "إن تستقمْ تسعدْ", grammaticalRule = "فعل شرط وجواب مجزومان بالسكون", category = "ARABIC_NAHW"),
                GrammarMatchPairEntity(wordOrPhrase = "By 2030, I will have graduated", grammaticalRule = "Future Perfect Tense", category = "ENGLISH_TENSES"),
                GrammarMatchPairEntity(wordOrPhrase = "Had you informed us earlier", grammaticalRule = "Inverted 3rd Conditional", category = "ENGLISH_TENSES"),
                GrammarMatchPairEntity(wordOrPhrase = "Rarely do we encounter bugs", grammaticalRule = "Negative Adverb Inversion", category = "ENGLISH_TENSES")
            )
            gamesDao.insertGrammarPairs(defaultPairs)
        }

        // 5. Seed Error Hunter Questions if empty
        if (gamesDao.getErrorHunterCount() == 0) {
            val defaultErrors = listOf(
                ErrorHunterQuestionEntity(
                    segmentA = "إنّ هذانِ العاملانِ",
                    segmentB = "مخلصان في أداء",
                    segmentC = "واجباتهما اليومية",
                    brokenSegmentIndex = 0,
                    explanation = "«هذين» اسم إن منصوب بالياء؛ فالصواب: إنّ هذين العاملين.",
                    correctedText = "إنّ هذين العاملين مخلصان في أداء واجباتهما اليومية",
                    languageRtl = true
                ),
                ErrorHunterQuestionEntity(
                    segmentA = "اجتهدوا في دراستكم",
                    segmentB = "لكي تحققون",
                    segmentC = "أعلى المراتب العلمية",
                    brokenSegmentIndex = 1,
                    explanation = "«تحققوا» مضارع منصوب بكي وعلامة نصبه حذف النون لأنه من الأفعال الخمسة.",
                    correctedText = "اجتهدوا في دراستكم لكي تحققوا أعلى المراتب العلمية",
                    languageRtl = true
                ),
                ErrorHunterQuestionEntity(
                    segmentA = "If the model was trained longer,",
                    segmentB = "it will achieve",
                    segmentC = "higher validation accuracy.",
                    brokenSegmentIndex = 1,
                    explanation = "In 2nd conditional ('was/were trained'), the main clause takes 'would achieve', not 'will'.",
                    correctedText = "If the model was trained longer, it would achieve higher validation accuracy.",
                    languageRtl = false
                ),
                ErrorHunterQuestionEntity(
                    segmentA = "ما كان الباحثون",
                    segmentB = "ليقصرون في إيجاد",
                    segmentC = "الحل التقني الأمثل",
                    brokenSegmentIndex = 1,
                    explanation = "«ليقصروا» مسبوق بلام الجحود الناصبة للمضارع، فينصب بحذف النون.",
                    correctedText = "ما كان الباحثون ليقصروا في إيجاد الحل التقني الأمثل",
                    languageRtl = true
                )
            )
            gamesDao.insertErrorHunterQuestions(defaultErrors)
        }
    }
}

data class AnswerResult(
    val isCorrect: Boolean,
    val strikesCount: Int,
    val penaltyActivated: Boolean,
    val explanation: String
)
