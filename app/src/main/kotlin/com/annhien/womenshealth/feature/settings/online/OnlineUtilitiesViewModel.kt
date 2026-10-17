package com.annhien.womenshealth.feature.settings.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.ratelimit.RateLimitManager
import com.annhien.womenshealth.core.ratelimit.RateLimitStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OnlineUtilitiesViewModel @Inject constructor(
    private val rateLimitManager: RateLimitManager
) : ViewModel() {

    val statuses: StateFlow<Map<String, RateLimitStatus>> = rateLimitManager.statusBySource
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    fun refresh() {
        rateLimitManager.clearExpired()
    }
}
