package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.GrammarMatchPairEntity
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

data class SpeedGameState(
    val pairs: List<GrammarMatchPairEntity> = emptyList(),
    val shuffledRules: List<GrammarMatchPairEntity> = emptyList(),
    val matchedIds: Set<Long> = emptySet(),
    val selectedPhraseId: Long? = null,
    val selectedRuleId: Long? = null,
    val score: Int = 0,
    val remainingSeconds: Int = 60,
    val isGameOver: Boolean = false,
    val feedbackMessage: String? = null
)

@HiltViewModel
class SpeedGrammarViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SpeedGameState())
    val state: StateFlow<SpeedGameState> = _state.asStateFlow()

    private var timerJob: Job? = null

    fun startGame() {
        viewModelScope.launch {
            loadNewRound(initialScore = 0, initialSeconds = 60)
            startTimer()
        }
    }

    private suspend fun loadNewRound(initialScore: Int, initialSeconds: Int) {
        val pairs = repository.getGrammarMatchPairs(4)
        val shuffled = pairs.shuffled()
        _state.value = SpeedGameState(
            pairs = pairs,
            shuffledRules = shuffled,
            matchedIds = emptySet(),
            selectedPhraseId = null,
            selectedRuleId = null,
            score = initialScore,
            remainingSeconds = initialSeconds,
            isGameOver = false
        )
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _state.value.remainingSeconds > 0) {
                delay(1000L)
                val newSeconds = _state.value.remainingSeconds - 1
                if (newSeconds <= 0) {
                    _state.value = _state.value.copy(remainingSeconds = 0, isGameOver = true)
                    break
                } else {
                    _state.value = _state.value.copy(remainingSeconds = newSeconds)
                }
            }
        }
    }

    fun selectPhrase(pairId: Long) {
        if (_state.value.isGameOver || _state.value.matchedIds.contains(pairId)) return
        _state.value = _state.value.copy(selectedPhraseId = pairId)
        checkMatch()
    }

    fun selectRule(pairId: Long) {
        if (_state.value.isGameOver || _state.value.matchedIds.contains(pairId)) return
        _state.value = _state.value.copy(selectedRuleId = pairId)
        checkMatch()
    }

    private fun checkMatch() {
        val current = _state.value
        val phraseId = current.selectedPhraseId
        val ruleId = current.selectedRuleId

        if (phraseId != null && ruleId != null) {
            if (phraseId == ruleId) {
                // Correct match!
                val updatedMatched = current.matchedIds + phraseId
                val newScore = current.score + 100

                if (updatedMatched.size == current.pairs.size) {
                    // All 4 pairs cleared, load next round immediately
                    viewModelScope.launch {
                        val pairs = repository.getGrammarMatchPairs(4)
                        _state.value = current.copy(
                            pairs = pairs,
                            shuffledRules = pairs.shuffled(),
                            matchedIds = emptySet(),
                            selectedPhraseId = null,
                            selectedRuleId = null,
                            score = newScore + 50, // Bonus for clearing board
                            feedbackMessage = "رائع! تطابق تام (+150)"
                        )
                    }
                } else {
                    _state.value = current.copy(
                        matchedIds = updatedMatched,
                        selectedPhraseId = null,
                        selectedRuleId = null,
                        score = newScore,
                        feedbackMessage = "إجابة صحيحة (+100)"
                    )
                }
            } else {
                // Wrong match!
                _state.value = current.copy(
                    selectedPhraseId = null,
                    selectedRuleId = null,
                    score = (current.score - 20).coerceAtLeast(0),
                    feedbackMessage = "خطأ في التوصيل (-20)"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
