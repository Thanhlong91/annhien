package com.annhien.womenshealth.feature.online.environment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.online.environment.OpenAqLocation
import com.annhien.womenshealth.core.online.environment.OpenAqRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AirQualityUiState {
    data object Idle : AirQualityUiState
    data object Loading : AirQualityUiState
    data class Success(
        val locations: List<OpenAqLocation>,
        val fromCache: Boolean
    ) : AirQualityUiState

    data class Error(val message: String) : AirQualityUiState
}

@HiltViewModel
class AirQualityViewModel @Inject constructor(
    private val repository: OpenAqRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AirQualityUiState>(AirQualityUiState.Idle)
    val uiState: StateFlow<AirQualityUiState> = _uiState.asStateFlow()

    fun loadNearbyLocations(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = AirQualityUiState.Loading

            when (val result = repository.getNearbyLocations(latitude, longitude)) {
                is OnlineResult.Success -> {
                    _uiState.value = AirQualityUiState.Success(
                        locations = result.data,
                        fromCache = result.fromCache
                    )
                }
                is OnlineResult.Error -> {
                    _uiState.value = AirQualityUiState.Error(
                        message = when (result) {
                            OnlineResult.Error.Offline -> "Không có kết nối Internet. Vui lòng thử lại khi có mạng."
                            OnlineResult.Error.RateLimited -> "Tiện ích này đã đạt giới hạn sử dụng. Vui lòng thử lại sau."
                            OnlineResult.Error.InvalidApiKey -> "Khóa API không hợp lệ."
                            OnlineResult.Error.NotFound -> "Không tìm thấy khu vực quan sát không khí gần bạn."
                            OnlineResult.Error.ServiceUnavailable -> "Dịch vụ đang tạm thời không khả dụng."
                            is OnlineResult.Error.Unknown -> result.message ?: "Không thể tải dữ liệu chất lượng không khí."
                        }
                    )
                }
            }
        }
    }
}
