package com.baccalaureate.studyenforcer.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.LessonEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val currentTab = MutableStateFlow(0) // 0: Curriculum Lessons, 1: Question Bank, 2: Mistakes Queue
    val selectedSubjectFilter = MutableStateFlow("ARABIC")
    val selectedTopicFilter = MutableStateFlow("ALL")

    val lessons: StateFlow<List<LessonEntity>> = selectedSubjectFilter.flatMapLatest { sub ->
        repository.getLessonsBySubject(if (sub == "ALL") "ARABIC" else sub)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userStats: StateFlow<UserStatsEntity?> = repository.getUserStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableTopics: StateFlow<List<String>> = selectedSubjectFilter.flatMapLatest { sub ->
        if (sub == "ALL") MutableStateFlow(emptyList())
        else repository.getTopicsForSubject(sub)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questions: StateFlow<List<QuestionEntity>> = combine(
        repository.getAllQuestions(),
        repository.getMistakeQuestions(),
        currentTab,
        selectedSubjectFilter,
        selectedTopicFilter
    ) { allQuestions, mistakeQuestions, tab, subject, topic ->
        val baseList = when (tab) {
            2 -> mistakeQuestions
            else -> allQuestions
        }
        baseList.filter { q ->
            (subject == "ALL" || q.subject == subject) &&
            (topic == "ALL" || q.topic == topic)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tabIndex: Int) {
        currentTab.value = tabIndex
    }

    fun setSubject(subject: String) {
        selectedSubjectFilter.value = subject
        selectedTopicFilter.value = "ALL"
    }

    fun setTopic(topic: String) {
        selectedTopicFilter.value = topic
    }
}
