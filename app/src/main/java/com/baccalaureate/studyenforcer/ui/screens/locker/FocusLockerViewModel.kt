package com.baccalaureate.studyenforcer.ui.screens.locker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.LockerStateEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.service.InstalledAppScanner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _deviceApps = MutableStateFlow<List<InstalledAppScanner.AppInfoItem>>(emptyList())
    val deviceApps: StateFlow<List<InstalledAppScanner.AppInfoItem>> = _deviceApps.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun selectDuration(minutes: Int) {
        selectedDurationMinutes.value = minutes
    }

    fun activateStrictLocker() {
        viewModelScope.launch {
            repository.activateLocker(selectedDurationMinutes.value)
        }
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val apps = repository.getDeviceAppsWithStatus()
                _deviceApps.value = apps
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.toggleAppBlocked(packageName, isBlocked)
            loadInstalledApps()
        }
    }

    fun autoBlockAllDistractions() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                repository.autoBlockAllDistractions()
                loadInstalledApps()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }
}
