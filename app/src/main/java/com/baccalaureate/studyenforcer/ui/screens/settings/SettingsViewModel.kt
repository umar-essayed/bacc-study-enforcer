package com.baccalaureate.studyenforcer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.BlockedAppEntity
import com.baccalaureate.studyenforcer.data.repository.StudyRepository
import com.baccalaureate.studyenforcer.service.LocalSyncServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: StudyRepository
) : ViewModel() {

    private val _serverIp = MutableStateFlow(LocalSyncServer.getLocalIpAddress())
    val serverIp: StateFlow<String> = _serverIp.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    val blockedApps: StateFlow<List<BlockedAppEntity>> = repository.getBlockedAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshIp() {
        _serverIp.value = LocalSyncServer.getLocalIpAddress()
    }

    fun importQuestionsFromJson(json: String) {
        viewModelScope.launch {
            _isImporting.value = true
            val result = repository.importQuestionsFromJson(json)
            val count = result.getOrDefault(0)
            _isImporting.value = false
            if (count > 0) {
                _statusMessage.value = "تم استيراد $count سؤالاً بنجاح إلى بنك الأسئلة"
            } else {
                _statusMessage.value = "تعذر الاستيراد، تحقق من تنسيق الـ JSON"
            }
        }
    }

    fun toggleAppBlocked(packageName: String, isBlocked: Boolean) {
        viewModelScope.launch {
            repository.toggleAppBlocked(packageName, isBlocked)
        }
    }

    fun resetStrikes() {
        viewModelScope.launch {
            repository.unlockPenaltyEarlyAfterReview()
            _statusMessage.value = "تمت إعادة ضبط عداد المخالفات"
        }
    }

    fun clearStatus() {
        _statusMessage.value = null
    }
}
