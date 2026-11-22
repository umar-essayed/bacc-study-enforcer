package com.baccalaureate.studyenforcer.data.repository

import android.content.Context
import com.baccalaureate.studyenforcer.data.local.AppDatabase
import com.baccalaureate.studyenforcer.data.local.entity.BlockedAppEntity
import com.baccalaureate.studyenforcer.data.local.entity.ErrorHunterQuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.GrammarMatchPairEntity
import com.baccalaureate.studyenforcer.data.local.entity.LessonEntity
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.SubjectProgressEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity
import com.baccalaureate.studyenforcer.service.InstalledAppScanner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val gson: Gson
) {
    private val questionDao = db.questionDao()
    private val lessonDao = db.lessonDao()
    private val userStatsDao = db.userStatsDao()
    private val blockedAppDao = db.blockedAppDao()
    private val lockerStateDao = db.lockerStateDao()
    private val gamesDao = db.gamesDao()
    private val appScanner = InstalledAppScanner(context)

    // --- Curriculum & Lessons API ---
    fun getLessonsBySubject(subject: String = "ARABIC"): Flow<List<LessonEntity>> =
        lessonDao.getLessonsBySubject(subject)

    fun getLessonByIdFlow(lessonId: Long): Flow<LessonEntity?> =
        lessonDao.getLessonByIdFlow(lessonId)

    suspend fun getLessonById(lessonId: Long): LessonEntity? =
        lessonDao.getLessonById(lessonId)

    suspend fun updateLessonProgress(lessonId: Long, isCompleted: Boolean, score: Int) {
        lessonDao.updateLessonProgress(lessonId, isCompleted, score)
        // Unlock next lesson in sequence
        val nextLessonId = when (lessonId) {
            101L -> 102L
            102L -> 103L
            103L -> 201L
            201L -> 202L
            202L -> 203L
            203L -> 301L
            301L -> 302L
            302L -> 303L
            else -> lessonId
        }
        if (nextLessonId != lessonId) {
            lessonDao.unlockLesson(nextLessonId)
            val currentStats = userStatsDao.getUserStats() ?: UserStatsEntity()
            if (nextLessonId > currentStats.unlockedLessonId) {
                userStatsDao.insertOrUpdateStats(
                    currentStats.copy(
                        currentLessonId = nextLessonId,
                        unlockedLessonId = nextLessonId
                    )
                )
            }
        }
        calculateAndUpdateMasteryScores()
    }

    // --- Questions API ---
    fun getAllQuestions(): Flow<List<QuestionEntity>> = questionDao.getAllQuestions()
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsBySubject(subject)

    fun getQuestionsBySubjectAndTopic(subject: String, topic: String): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsBySubjectAndTopic(subject, topic)

    fun getQuestionsByLesson(lessonId: Long): Flow<List<QuestionEntity>> =
        questionDao.getQuestionsByLesson(lessonId)

    suspend fun getQuestionById(id: Long): QuestionEntity? =
        questionDao.getQuestionById(id)

    suspend fun addQuestionDirectly(question: QuestionEntity) {
        questionDao.insertQuestion(question)
    }

    suspend fun recordQuestionAnswer(questionId: Long, isCorrect: Boolean) {
        questionDao.markQuestionAnswered(questionId, isCorrect)
    }

    fun getQuestionsFiltered(subject: String = "ALL", topic: String? = null): Flow<List<QuestionEntity>> {
        return when {
            subject == "ALL" -> getAllQuestions()
            topic.isNullOrBlank() -> getQuestionsBySubject(subject)
            else -> getQuestionsBySubjectAndTopic(subject, topic)
        }
    }

    fun getTopicsForSubject(subject: String): Flow<List<String>> =
        questionDao.getTopicsForSubject(subject)

    fun getMistakeQuestions(): Flow<List<QuestionEntity>> = questionDao.getMistakeQuestions()

    fun getMistakeQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> =
        questionDao.getMistakeQuestionsBySubject(subject)

    /**
     * Smart Non-Repeating Question Selector:
     * 1. Priority: Unanswered questions within student unlocked curriculum scope
     * 2. Priority: Mistakes to reinforce learning
     * 3. Priority: Least shown questions
     * Guaranteed never to repeat the current question immediately.
     */
    suspend fun getSmartNextQuestion(subject: String? = null, excludeId: Long? = null): QuestionEntity? {
        val stats = userStatsDao.getUserStats()
        val maxLesson = stats?.unlockedLessonId ?: 9999L

        // 1. Try unseen / unanswered questions
        var q = if (!subject.isNullOrBlank() && subject != "ALL") {
            questionDao.getNextUnansweredQuestionBySubject(subject, maxLesson)
        } else {
            questionDao.getNextUnansweredQuestion(maxLesson)
        }

        if (q != null && q.id != excludeId) {
            questionDao.incrementTimesShown(q.id)
            return q
        }

        // 2. Try mistake questions
        q = questionDao.getNextMistakeQuestion(maxLesson)
        if (q != null && q.id != excludeId) {
            questionDao.incrementTimesShown(q.id)
            return q
        }

        // 3. Fallback to least shown questions
        q = questionDao.getNextQuestionOrdered(maxLesson)
        if (q != null && q.id != excludeId) {
            questionDao.incrementTimesShown(q.id)
            return q
        }

        val fallback = questionDao.getRandomQuestion()
        if (fallback != null) {
            questionDao.incrementTimesShown(fallback.id)
        }
        return fallback
    }

    suspend fun getRandomQuestion(): QuestionEntity? = getSmartNextQuestion()

    suspend fun getRandomQuestionBySubject(subject: String): QuestionEntity? =
        getSmartNextQuestion(subject = subject)

    suspend fun getRandomQuestionBySubjectAndTopic(subject: String, topic: String): QuestionEntity? =
        questionDao.getRandomQuestionBySubjectAndTopic(subject, topic)

    // --- Stats & User Progress ---
    fun getUserStatsFlow(): Flow<UserStatsEntity?> = userStatsDao.getUserStatsFlow()
    fun getAllSubjectProgress(): Flow<List<SubjectProgressEntity>> =
        userStatsDao.getAllSubjectProgress()

    fun getLockerStateFlow(): Flow<LockerStateEntity?> = lockerStateDao.getLockerStateFlow()
    suspend fun getLockerState(): LockerStateEntity? = lockerStateDao.getLockerState()

    // --- Blocked Apps Management ---
    fun getBlockedAppsFlow(): Flow<List<BlockedAppEntity>> = blockedAppDao.getAllBlockedApps()
    suspend fun isAppBlocked(packageName: String): Boolean = blockedAppDao.isAppBlocked(packageName)

    suspend fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        blockedAppDao.updateAppBlockedStatus(packageName, isBlocked)
    }

    suspend fun addCustomBlockedApp(packageName: String, appName: String) {
        blockedAppDao.insertApp(BlockedAppEntity(packageName = packageName, appName = appName, isBlocked = true))
    }

    suspend fun getDeviceAppsWithStatus(): List<InstalledAppScanner.AppInfoItem> = withContext(Dispatchers.IO) {
        val currentBlocked = blockedAppDao.getActiveBlockedApps().map { it.packageName }.toSet()
        appScanner.getInstalledDeviceApps(currentBlocked)
    }

    suspend fun autoBlockAllDistractions(): Int = withContext(Dispatchers.IO) {
        val apps = getDeviceAppsWithStatus()
        val distractions = apps.filter { it.isDistraction }
        val entities = distractions.map {
            BlockedAppEntity(packageName = it.packageName, appName = it.appName, isBlocked = true)
        }
        blockedAppDao.insertApps(entities)
        distractions.size
    }

    suspend fun completeOnboarding(studentName: String) {
        val currentStats = userStatsDao.getUserStats() ?: UserStatsEntity()
        userStatsDao.insertOrUpdateStats(
            currentStats.copy(
                userName = studentName.ifBlank { "بطل الثانوية" },
                isOnboardingCompleted = true
            )
        )
    }

    suspend fun importQuestionsFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val type = object : TypeToken<List<QuestionEntity>>() {}.type
            val list: List<QuestionEntity> = gson.fromJson(jsonString, type)
            if (list.isNotEmpty()) {
                questionDao.insertQuestions(list)
                Result.success(list.size)
            } else {
                Result.failure(IllegalArgumentException("قائمة الأسئلة فارغة"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordAnswer(question: QuestionEntity, selectedOptionIndex: Int): AnswerResult {
        val isCorrect = question.correctOptionIndex == selectedOptionIndex

        questionDao.markQuestionAnswered(question.id, isCorrect)
        userStatsDao.recordAnswer(question.subject, isCorrect)

        calculateAndUpdateMasteryScores()

        val updatedStats = userStatsDao.getUserStats() ?: UserStatsEntity()
        if (updatedStats.strikesCount >= 3) {
            activatePenaltyMode(60, "تجاوز 3 ضربات متتالية في الاختبار")
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

    private suspend fun calculateAndUpdateMasteryScores() = withContext(Dispatchers.IO) {
        val stats = userStatsDao.getUserStats() ?: return@withContext
        val allProgress = userStatsDao.getAllSubjectProgress().firstOrNull() ?: emptyList()

        var arabicAcc = 0f
        var englishAcc = 0f
        var historyAcc = 0f
        var programmingAcc = 0f

        allProgress.forEach { prog ->
            when (prog.subject) {
                "ARABIC" -> arabicAcc = prog.accuracy
                "ENGLISH" -> englishAcc = prog.accuracy
                "HISTORY" -> historyAcc = prog.accuracy
                "PROGRAMMING" -> programmingAcc = prog.accuracy
            }
        }

        val totalAnswered = stats.totalAnswered
        val correctCount = stats.correctCount
        val overallAcc = if (totalAnswered > 0) (correctCount.toFloat() / totalAnswered.toFloat()) * 100f else 0f

        userStatsDao.insertOrUpdateStats(
            stats.copy(
                overallMasteryScore = overallAcc,
                arabicScore = arabicAcc,
                englishScore = englishAcc,
                historyScore = historyAcc,
                programmingScore = programmingAcc
            )
        )
    }

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
        userStatsDao.updateStrikes(0)
        lockerStateDao.clearLock()
    }

    suspend fun consumeSnoozeToken(): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        var stats = userStatsDao.getUserStats() ?: UserStatsEntity()

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

    // --- Games API ---
    suspend fun getGrammarMatchPairs(limit: Int = 4): List<GrammarMatchPairEntity> =
        gamesDao.getRandomGrammarPairs(limit)

    suspend fun getErrorHunterQuestions(limit: Int = 10): List<ErrorHunterQuestionEntity> =
        gamesDao.getRandomErrorHunterQuestions(limit)

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Seed Curriculum Lessons
        if (lessonDao.getLessonCount() == 0) {
            try {
                context.assets.open("seed_curriculum.json").use { stream ->
                    val reader = InputStreamReader(stream)
                    val type = object : TypeToken<List<LessonEntity>>() {}.type
                    val lessons: List<LessonEntity> = gson.fromJson(reader, type)
                    lessonDao.insertLessons(lessons)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Seed Real Curriculum Questions
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

        // 3. Seed User Stats
        if (userStatsDao.getUserStats() == null) {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            userStatsDao.insertOrUpdateStats(
                UserStatsEntity(
                    id = 1,
                    userName = "طالب الثانوية",
                    isOnboardingCompleted = false,
                    streakDays = 1,
                    totalAnswered = 0,
                    correctCount = 0,
                    strikesCount = 0,
                    snoozeTokensRemaining = 2,
                    lastSnoozeResetDate = today,
                    totalFocusMinutes = 0,
                    lastActiveDate = today,
                    currentLessonId = 101,
                    unlockedLessonId = 101
                )
            )

            listOf("ARABIC", "ENGLISH", "HISTORY", "PROGRAMMING").forEach { sub ->
                userStatsDao.insertSubjectProgress(
                    SubjectProgressEntity(
                        subject = sub,
                        totalQuestions = if (sub == "ARABIC") 16 else 5,
                        answeredCount = 0,
                        correctCount = 0
                    )
                )
            }
        }

        // 4. Seed and Auto-Detect Blocked Apps (Always include WhatsApp, Telegram, etc.)
        val defaultBlocked = listOf(
            BlockedAppEntity("com.whatsapp", "WhatsApp", true),
            BlockedAppEntity("com.whatsapp.w4b", "WhatsApp Business", true),
            BlockedAppEntity("org.telegram.messenger", "Telegram", true),
            BlockedAppEntity("com.facebook.katana", "Facebook", true),
            BlockedAppEntity("com.facebook.orca", "Messenger", true),
            BlockedAppEntity("com.instagram.android", "Instagram", true),
            BlockedAppEntity("com.zhiliaoapp.musically", "TikTok", true),
            BlockedAppEntity("com.google.android.youtube", "YouTube", true),
            BlockedAppEntity("com.twitter.android", "X (Twitter)", true),
            BlockedAppEntity("com.reddit.frontpage", "Reddit", true),
            BlockedAppEntity("com.snapchat.android", "Snapchat", true),
            BlockedAppEntity("com.discord", "Discord", true)
        )
        blockedAppDao.insertApps(defaultBlocked)

        // Auto scan and add any installed distractions dynamically
        try {
            autoBlockAllDistractions()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Seed Games content
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
