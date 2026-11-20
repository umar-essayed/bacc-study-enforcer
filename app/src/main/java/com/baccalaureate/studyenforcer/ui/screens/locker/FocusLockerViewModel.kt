package com.baccalaureate.studyenforcer.ui.screens.locker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FocusLockerViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    val selectedDurationMinutes = MutableStateFlow(60)

    val lockerState: StateFlow<LockerStateEntity?> = repository.getLockerStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDuration(minutes: Int) {
        selectedDurationMinutes.value = minutes
    }

    fun activateStrictLocker() {
        viewModelScope.launch {
            repository.activateLocker(selectedDurationMinutes.value)
        }
    }
}
