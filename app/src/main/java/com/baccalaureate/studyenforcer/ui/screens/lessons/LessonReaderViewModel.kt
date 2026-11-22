package com.baccalaureate.studyenforcer.ui.screens.lessons

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.LessonEntity
import com.baccalaureate.studyenforcer.data.local.entity.QuestionEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonReaderViewModel @Inject constructor(
    private val repository: StudyRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val lessonId: Long = savedStateHandle.get<String>("lessonId")?.toLongOrNull() ?: 101L

    val lesson: StateFlow<LessonEntity?> = repository.getLessonByIdFlow(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val questions: StateFlow<List<QuestionEntity>> = repository.getQuestionsByLesson(lessonId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markLessonCompleted(score: Int) {
        viewModelScope.launch {
            repository.updateLessonProgress(lessonId, isCompleted = true, score = score)
        }
    }
}
