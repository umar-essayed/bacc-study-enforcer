package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.ErrorHunterQuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ErrorHunterState(
    val questions: List<ErrorHunterQuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val selectedSegment: Int? = null,
    val isSubmitted: Boolean = false,
    val remainingSeconds: Int = 10,
    val score: Int = 0
) {
    val currentQuestion: ErrorHunterQuestionEntity?
        get() = questions.getOrNull(currentIndex)
}

@HiltViewModel
class ErrorHunterViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ErrorHunterState())
    val state: StateFlow<ErrorHunterState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun startGame() {
        viewModelScope.launch {
            val questions = repository.getErrorHunterQuestions(10)
            _state.value = ErrorHunterState(
                questions = questions,
                currentIndex = 0,
                selectedSegment = null,
                isSubmitted = false,
                remainingSeconds = 10,
                score = 0
            )
            start10SecondTimer()
        }
    }

    private fun start10SecondTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0 && !_state.value.isSubmitted) {
                delay(1000L)
                val newSec = _state.value.remainingSeconds - 1
                if (newSec <= 0) {
                    // Time out
                    _state.value = _state.value.copy(
                        remainingSeconds = 0,
                        isSubmitted = true
                    )
                    break
                } else {
                    _state.value = _state.value.copy(remainingSeconds = newSec)
                }
            }
        }
    }

    fun selectSegment(segmentIndex: Int) {
        if (_state.value.isSubmitted) return
        countdownJob?.cancel()

        val q = _state.value.currentQuestion ?: return
        val isCorrectCatch = segmentIndex == q.brokenSegmentIndex
        val addedScore = if (isCorrectCatch) 100 else 0

        _state.value = _state.value.copy(
            selectedSegment = segmentIndex,
            isSubmitted = true,
            score = _state.value.score + addedScore
        )
    }

    fun nextQuestion() {
        val nextIdx = _state.value.currentIndex + 1
        if (nextIdx < _state.value.questions.size) {
            _state.value = _state.value.copy(
                currentIndex = nextIdx,
                selectedSegment = null,
                isSubmitted = false,
                remainingSeconds = 10
            )
            start10SecondTimer()
        } else {
            // End of run, restart or reload
            startGame()
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
