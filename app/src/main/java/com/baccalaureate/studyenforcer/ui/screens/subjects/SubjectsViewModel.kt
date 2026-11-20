package com.baccalaureate.studyenforcer.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val selectedSubjectFilter = MutableStateFlow("ALL")

    val questions: StateFlow<List<QuestionEntity>> = combine(
        repository.getAllQuestions(),
        selectedSubjectFilter
    ) { allQuestions, filter ->
        if (filter == "ALL") allQuestions
        else allQuestions.filter { it.subject == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: String) {
        selectedSubjectFilter.value = filter
    }
}
