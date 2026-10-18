package com.annhien.womenshealth.feature.online.environment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.location.LocationData
import com.annhien.womenshealth.core.location.LocationProvider
import com.annhien.womenshealth.core.online.environment.WaqiData
import com.annhien.womenshealth.core.online.environment.WaqiRepository
import com.annhien.womenshealth.core.preferences.OnlineUtilityPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AirQualityUiState {
    data object Idle : AirQualityUiState
    data object Loading : AirQualityUiState
    data class Success(
        val data: WaqiData,
        val fromCache: Boolean
    ) : AirQualityUiState

    data class Error(val message: String, val isTokenMissing: Boolean = false) : AirQualityUiState
}

@HiltViewModel
class AirQualityViewModel @Inject constructor(
    private val waqiRepository: WaqiRepository,
    private val locationProvider: LocationProvider,
    private val preferences: OnlineUtilityPreferences? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<AirQualityUiState>(AirQualityUiState.Idle)
    val uiState: StateFlow<AirQualityUiState> = _uiState.asStateFlow()

    val aqicnToken: StateFlow<String> = preferences?.getAqicnToken()
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
        ?: MutableStateFlow("")

    fun saveToken(token: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            preferences?.setAqicnToken(token.trim())
            loadAirQuality(latitude, longitude)
        }
    }

    fun loadAirQuality(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = AirQualityUiState.Loading

            when (val result = waqiRepository.getFeedByGeo(latitude, longitude)) {
                is OnlineResult.Success -> {
                    _uiState.value = AirQualityUiState.Success(
                        data = result.data,
                        fromCache = result.fromCache
                    )
                }
                is OnlineResult.Error -> {
                    val isMissingToken = result is OnlineResult.Error.InvalidApiKey
                    _uiState.value = AirQualityUiState.Error(
                        message = when (result) {
                            OnlineResult.Error.Offline -> "Không có kết nối mạng. Đang hiển thị dữ liệu lưu tạm nếu có."
                            OnlineResult.Error.RateLimited -> "Đã đạt giới hạn yêu cầu (Rate Limited) tới hệ thống AQICN. Vui lòng thử lại sau giây lát."
                            OnlineResult.Error.InvalidApiKey -> "Token AQICN chưa hợp lệ hoặc chưa được kích hoạt."
                            OnlineResult.Error.NotFound -> "Không tìm thấy trạm quan trắc không khí gần tọa độ này."
                            OnlineResult.Error.ServiceUnavailable -> "Hệ thống trạm quan trắc AQICN đang bảo trì. Vui lòng thử lại sau."
                            is OnlineResult.Error.Unknown -> result.message ?: "Không thể tải dữ liệu chất lượng không khí."
                        },
                        isTokenMissing = isMissingToken
                    )
                }
            }
        }
    }

    fun loadAirQualityByCity(city: String) {
        if (city.isBlank()) return
        viewModelScope.launch {
            _uiState.value = AirQualityUiState.Loading

            when (val result = waqiRepository.getFeedByCity(city.trim())) {
                is OnlineResult.Success -> {
                    _uiState.value = AirQualityUiState.Success(
                        data = result.data,
                        fromCache = result.fromCache
                    )
                }
                is OnlineResult.Error -> {
                    _uiState.value = AirQualityUiState.Error(
                        message = when (result) {
                            OnlineResult.Error.Offline -> "Không có kết nối mạng."
                            OnlineResult.Error.RateLimited -> "Đã đạt giới hạn truy vấn."
                            OnlineResult.Error.InvalidApiKey -> "Token AQICN chưa hợp lệ."
                            OnlineResult.Error.NotFound -> "Không tìm thấy trạm quan trắc cho thành phố '$city'."
                            OnlineResult.Error.ServiceUnavailable -> "Hệ thống AQICN đang bận."
                            is OnlineResult.Error.Unknown -> result.message ?: "Lỗi tải dữ liệu cho '$city'."
                        },
                        isTokenMissing = result is OnlineResult.Error.InvalidApiKey
                    )
                }
            }
        }
    }

    fun loadAirQualityUsingDeviceLocation() {
        if (!locationProvider.hasLocationPermission()) {
            _uiState.value = AirQualityUiState.Error(
                message = "Cần quyền vị trí GPS để tìm trạm quan trắc không khí gần bạn nhất."
            )
            return
        }

        _uiState.value = AirQualityUiState.Loading

        locationProvider.currentLocation()
            .catch { throwable ->
                _uiState.value = AirQualityUiState.Error(
                    message = throwable.message ?: "Không thể xác định vị trí hiện tại."
                )
            }
            .onEach { location: LocationData ->
                loadAirQuality(location.latitude, location.longitude)
            }
            .launchIn(viewModelScope)
    }
}
