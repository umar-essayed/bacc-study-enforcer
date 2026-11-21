package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class TrueFalseItem(
    val statement: String,
    val isTrue: Boolean,
    val explanation: String,
    val subject: String,
    val topic: String
)

data class TrueFalseGameState(
    val currentItem: TrueFalseItem? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val remainingSeconds: Int = 30,
    val isGameOver: Boolean = false,
    val lastAnswerWasCorrect: Boolean? = null,
    val lastExplanation: String? = null
)

@HiltViewModel
class TrueFalseViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val gson: Gson
) : ViewModel() {

    private val _state = MutableStateFlow(TrueFalseGameState())
    val state: StateFlow<TrueFalseGameState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private var allItems: List<TrueFalseItem> = emptyList()
    private var itemIndex = 0

    fun startGame(subject: String = "ALL", topic: String? = null) {
        viewModelScope.launch {
            val questions = if (subject == "ALL") {
                repository.getAllQuestions().first()
            } else {
                repository.getQuestionsBySubjectAndTopic(subject, topic).first()
            }

            if (questions.isEmpty()) {
                val fallback = repository.getAllQuestions().first()
                allItems = convertQuestionsToTF(fallback).shuffled()
            } else {
                allItems = convertQuestionsToTF(questions).shuffled()
            }

            itemIndex = 0
            _state.value = TrueFalseGameState(
                currentItem = allItems.getOrNull(itemIndex),
                score = 0,
                streak = 0,
                correctCount = 0,
                wrongCount = 0,
                remainingSeconds = 30,
                isGameOver = false
            )

            startTimer()
        }
    }

    private fun convertQuestionsToTF(questions: List<QuestionEntity>): List<TrueFalseItem> {
        val listType = object : TypeToken<List<String>>() {}.type
        return questions.map { q ->
            val options: List<String> = try {
                gson.fromJson(q.optionsJson, listType) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }

            val makeTrue = Random.nextBoolean() || options.size <= 1
            val chosenOption: String
            val isTrue: Boolean

            if (makeTrue && options.isNotEmpty()) {
                chosenOption = options.getOrElse(q.correctOptionIndex) { options[0] }
                isTrue = true
            } else if (options.isNotEmpty()) {
                val wrongIndices = options.indices.filter { it != q.correctOptionIndex }
                val wrongIdx = if (wrongIndices.isNotEmpty()) wrongIndices.random() else q.correctOptionIndex
                chosenOption = options[wrongIdx]
                isTrue = (wrongIdx == q.correctOptionIndex)
            } else {
                chosenOption = "صواب"
                isTrue = true
            }

            TrueFalseItem(
                statement = "${q.questionText}\n← [$chosenOption]",
                isTrue = isTrue,
                explanation = q.explanation,
                subject = q.subject,
                topic = q.topic
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0) {
                delay(1000L)
                val newSec = _state.value.remainingSeconds - 1
                if (newSec <= 0) {
                    _state.value = _state.value.copy(remainingSeconds = 0, isGameOver = true)
                    break
                } else {
                    _state.value = _state.value.copy(remainingSeconds = newSec)
                }
            }
        }
    }

    fun submitAnswer(userChoice: Boolean) {
        if (_state.value.isGameOver) return
        val current = _state.value.currentItem ?: return

        val isCorrect = (userChoice == current.isTrue)
        val newStreak = if (isCorrect) _state.value.streak + 1 else 0
        val points = if (isCorrect) 100 + (newStreak * 10) else 0

        itemIndex++
        val nextItem = allItems.getOrNull(itemIndex)

        _state.value = _state.value.copy(
            score = _state.value.score + points,
            streak = newStreak,
            correctCount = _state.value.correctCount + (if (isCorrect) 1 else 0),
            wrongCount = _state.value.wrongCount + (if (!isCorrect) 1 else 0),
            lastAnswerWasCorrect = isCorrect,
            lastExplanation = current.explanation,
            currentItem = nextItem,
            isGameOver = (nextItem == null)
        )
    }

    fun restart() {
        startGame()
    }
}
