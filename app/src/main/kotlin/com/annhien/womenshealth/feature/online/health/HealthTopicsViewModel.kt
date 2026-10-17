package com.annhien.womenshealth.feature.online.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.online.health.MyHealthfinderRepository
import com.annhien.womenshealth.core.online.health.MyHealthfinderTopic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HealthTopicsUiState {
    data object Loading : HealthTopicsUiState
    data class Success(
        val topics: List<MyHealthfinderTopic>,
        val fromCache: Boolean
    ) : HealthTopicsUiState

    data class Error(val message: String) : HealthTopicsUiState
}

@HiltViewModel
class HealthTopicsViewModel @Inject constructor(
    private val repository: MyHealthfinderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthTopicsUiState>(HealthTopicsUiState.Loading)
    val uiState: StateFlow<HealthTopicsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = HealthTopicsUiState.Loading

            when (val result = repository.getHealthTopics()) {
                is OnlineResult.Success -> {
                    _uiState.value = HealthTopicsUiState.Success(
                        topics = result.data,
                        fromCache = result.fromCache
                    )
                }
                is OnlineResult.Error -> {
                    _uiState.value = HealthTopicsUiState.Error(
                        message = when (result) {
                            OnlineResult.Error.Offline -> "Không có kết nối Internet. Vui lòng thử lại khi có mạng."
                            OnlineResult.Error.RateLimited -> "Tiện ích này đã đạt giới hạn sử dụng. Vui lòng thử lại sau."
                            OnlineResult.Error.InvalidApiKey -> "Khóa API không hợp lệ."
                            OnlineResult.Error.NotFound -> "Không tìm thấy chủ đề sức khỏe."
                            OnlineResult.Error.ServiceUnavailable -> "Dịch vụ đang tạm thời không khả dụng."
                            is OnlineResult.Error.Unknown -> result.message ?: "Không thể tải kiến thức sức khỏe."
                        }
                    )
                }
            }
        }
    }
}
