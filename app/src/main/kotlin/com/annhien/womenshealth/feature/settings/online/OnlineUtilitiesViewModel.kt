package com.annhien.womenshealth.feature.settings.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.preferences.OnlineUtilityPreferences
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import com.annhien.womenshealth.core.ratelimit.RateLimitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnlineUtilityUiState(
    val sourceId: String,
    val titleVi: String,
    val descriptionVi: String,
    val isEnabled: Boolean,
    val status: RateLimitStatus
)

@HiltViewModel
class OnlineUtilitiesViewModel @Inject constructor(
    private val rateLimitManager: RateLimitManager,
    private val onlineUtilityPreferences: OnlineUtilityPreferences
) : ViewModel() {

    private val enabledStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val utilities: StateFlow<List<OnlineUtilityUiState>> =
        combine(
            rateLimitManager.statusBySource,
            enabledStates.asStateFlow()
        ) { statuses, enabled ->
            OnlineUtilityCatalog.utilities.map { item ->
                OnlineUtilityUiState(
                    sourceId = item.sourceId,
                    titleVi = item.titleVi,
                    descriptionVi = item.descriptionVi,
                    isEnabled = enabled[item.sourceId] ?: true,
                    status = statuses[item.sourceId] ?: RateLimitStatus()
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OnlineUtilityCatalog.utilities.map {
                OnlineUtilityUiState(
                    sourceId = it.sourceId,
                    titleVi = it.titleVi,
                    descriptionVi = it.descriptionVi,
                    isEnabled = true,
                    status = RateLimitStatus()
                )
            }
        )

    init {
        OnlineUtilityCatalog.utilities.forEach { utility ->
            viewModelScope.launch {
                onlineUtilityPreferences.isEnabled(utility.sourceId).collect { enabled ->
                    enabledStates.value = enabledStates.value + (utility.sourceId to enabled)
                    if (!enabled) {
                        rateLimitManager.markDisabled(
                            sourceId = utility.sourceId,
                            message = "Tiện ích này đã được tắt trong Cài đặt."
                        )
                    } else {
                        rateLimitManager.markAvailable(utility.sourceId)
                    }
                }
            }
        }
    }

    fun setEnabled(sourceId: String, enabled: Boolean) {
        viewModelScope.launch {
            onlineUtilityPreferences.setEnabled(sourceId, enabled)
        }
    }

    fun refresh() {
        rateLimitManager.clearExpired()
    }
}
