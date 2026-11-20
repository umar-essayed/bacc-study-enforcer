package com.baccalaureate.studyenforcer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baccalaureate.studyenforcer.data.local.entity.BlockedAppEntity
import com.baccalaureate.studyenforcer.data.remote.SupabaseApi
import com.baccalaureate.studyenforcer.data.remote.SupabaseConfig
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
class SettingsViewModel @Inject constructor(
    private val repository: StudyRepository,
    private val supabaseConfig: SupabaseConfig,
    private val supabaseApi: SupabaseApi
) : ViewModel() {

    val supabaseUrl = MutableStateFlow(supabaseConfig.url)
    val supabaseAnonKey = MutableStateFlow(supabaseConfig.anonKey)

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus: StateFlow<String?> = _syncStatus.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val blockedApps: StateFlow<List<BlockedAppEntity>> = repository.getBlockedAppsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSupabaseConfig(url: String, key: String) {
        supabaseConfig.url = url
        supabaseConfig.anonKey = key
        supabaseUrl.value = url
        supabaseAnonKey.value = key
        _syncStatus.value = "تم حفظ بيانات Supabase بنجاح"
    }

    fun testConnection() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "جاري اختبار الاتصال بسيرفر Supabase..."
            val result = supabaseApi.testConnection()
            _isSyncing.value = false
            _syncStatus.value = if (result.isSuccess) {
                "✓ تم الاتصال بسيرفر Supabase بنجاح!"
            } else {
                "✗ فشل الاتصال: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncStatus.value = "جاري جلب وتحديث بنك الأسئلة من Supabase..."
            val result = repository.syncWithSupabase()
            _isSyncing.value = false
            _syncStatus.value = if (result.isSuccess) {
                "✓ تمت المزامنة بنجاح! تم تحميل ${result.getOrNull()} سؤالاً جديداً."
            } else {
                "✗ تعذرت المزامنة: ${result.exceptionOrNull()?.localizedMessage}"
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
            _syncStatus.value = "تمت إعادة ضبط عداد الضربات إلى 0"
        }
    }
}
