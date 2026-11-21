package com.baccalaureate.studyenforcer.ui.screens.today

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.local.entity.SubjectProgressEntity
import com.baccalaureate.studyenforcer.data.local.entity.UserStatsEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.service.StudyEnforcementService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val userStats: StateFlow<UserStatsEntity?> = repository.getUserStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjectProgress: StateFlow<List<SubjectProgressEntity>> = repository.getAllSubjectProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lockerState: StateFlow<LockerStateEntity?> = repository.getLockerStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun triggerQuickPopup(context: Context) {
        val intent = Intent(context, StudyEnforcementService::class.java).apply {
            putExtra(StudyEnforcementService.EXTRA_TRIGGER_NOW, true)
        }
        context.startService(intent)
    }
}
