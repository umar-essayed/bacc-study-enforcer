package com.baccalaureate.studyenforcer.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectOption(
    val id: String,
    val title: String
)

@HiltViewModel
class GamesViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val subjects = listOf(
        SubjectOption("ALL", "الكل"),
        SubjectOption("ARABIC", "النحو العربي"),
        SubjectOption("ENGLISH", "اللغة الإنجليزية"),
        SubjectOption("HISTORY", "التاريخ"),
        SubjectOption("PROGRAMMING", "البرمجة والذكاء الاصطناعي")
    )

    private val _selectedSubject = MutableStateFlow("ALL")
    val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

    private val _selectedTopic = MutableStateFlow<String?>(null)
    val selectedTopic: StateFlow<String?> = _selectedTopic.asStateFlow()

    private val _availableTopics = MutableStateFlow<List<String>>(emptyList())
    val availableTopics: StateFlow<List<String>> = _availableTopics.asStateFlow()

    fun selectSubject(subjectId: String) {
        _selectedSubject.value = subjectId
        _selectedTopic.value = null
        if (subjectId == "ALL") {
            _availableTopics.value = emptyList()
        } else {
            viewModelScope.launch {
                repository.getTopicsForSubject(subjectId).collect { topics ->
                    _availableTopics.value = topics
                }
            }
        }
    }

    fun selectTopic(topic: String?) {
        _selectedTopic.value = if (_selectedTopic.value == topic) null else topic
    }
}
