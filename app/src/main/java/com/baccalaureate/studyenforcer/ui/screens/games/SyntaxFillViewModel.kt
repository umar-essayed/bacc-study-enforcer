package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyntaxFillQuestion(
    val id: Long,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val subject: String,
    val topic: String
)

data class SyntaxFillState(
    val questions: List<SyntaxFillQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val streak: Int = 0,
    val isComplete: Boolean = false
) {
    val currentQuestion: SyntaxFillQuestion?
        get() = questions.getOrNull(currentIndex)
}

@HiltViewModel
class SyntaxFillViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SyntaxFillState())
    val state: StateFlow<SyntaxFillState> = _state.asStateFlow()

    fun startGame(subject: String = "ALL", topic: String? = null) {
        viewModelScope.launch {
            val rawQuestions = repository.getQuestionsFiltered(subject, topic).first()

            val effectiveQuestions = if (rawQuestions.isEmpty()) {
                repository.getAllQuestions().first()
            } else {
                rawQuestions
            }

            val fillQuestions = effectiveQuestions.mapNotNull { q ->
                val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD).filter { it.isNotBlank() }

                if (options.size >= 2) {
                    SyntaxFillQuestion(
                        id = q.id,
                        prompt = q.questionText,
                        options = options,
                        correctIndex = q.correctOptionIndex,
                        explanation = q.explanation,
                        subject = q.subject,
                        topic = q.topic
                    )
                } else null
            }.shuffled()

            _state.value = SyntaxFillState(
                questions = fillQuestions,
                currentIndex = 0,
                selectedOptionIndex = null,
                isSubmitted = false,
                score = 0,
                streak = 0,
                isComplete = fillQuestions.isEmpty()
            )
        }
    }

    fun selectOption(index: Int) {
        if (_state.value.isSubmitted) return
        val currentQ = _state.value.currentQuestion ?: return

        val isCorrect = (index == currentQ.correctIndex)
        val newStreak = if (isCorrect) _state.value.streak + 1 else 0
        val points = if (isCorrect) 100 + (newStreak * 10) else 0

        viewModelScope.launch {
            repository.recordQuestionAnswer(currentQ.id, isCorrect)
        }

        _state.value = _state.value.copy(
            selectedOptionIndex = index,
            isSubmitted = true,
            score = _state.value.score + points,
            streak = newStreak
        )
    }

    fun nextQuestion() {
        val nextIdx = _state.value.currentIndex + 1
        if (nextIdx < _state.value.questions.size) {
            _state.value = _state.value.copy(
                currentIndex = nextIdx,
                selectedOptionIndex = null,
                isSubmitted = false
            )
        } else {
            _state.value = _state.value.copy(isComplete = true)
        }
    }

    fun restart() {
        startGame()
    }
}
