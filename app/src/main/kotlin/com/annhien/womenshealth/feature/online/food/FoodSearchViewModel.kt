package com.annhien.womenshealth.feature.online.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.online.food.OpenFoodFactsProduct
import com.annhien.womenshealth.core.online.food.OpenFoodFactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FoodSearchUiState {
    data object Idle : FoodSearchUiState
    data object Loading : FoodSearchUiState
    data class Success(
        val products: List<OpenFoodFactsProduct>,
        val fromCache: Boolean
    ) : FoodSearchUiState

    data class Error(val message: String) : FoodSearchUiState
}

@HiltViewModel
class FoodSearchViewModel @Inject constructor(
    private val repository: OpenFoodFactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FoodSearchUiState>(FoodSearchUiState.Idle)
    val uiState: StateFlow<FoodSearchUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            _uiState.value = FoodSearchUiState.Error("Vui lòng nhập tên thực phẩm cần tìm")
            return
        }

        viewModelScope.launch {
            _uiState.value = FoodSearchUiState.Loading

            when (val result = repository.searchFood(trimmed)) {
                is OnlineResult.Success -> {
                    _uiState.value = FoodSearchUiState.Success(
                        products = result.data,
                        fromCache = result.fromCache
                    )
                }
                is OnlineResult.Error -> {
                    _uiState.value = FoodSearchUiState.Error(
                        message = when (result) {
                            OnlineResult.Error.Offline -> "Không có kết nối Internet. Vui lòng thử lại khi có mạng."
                            OnlineResult.Error.RateLimited -> "Tiện ích này đã đạt giới hạn sử dụng. Vui lòng thử lại sau."
                            OnlineResult.Error.InvalidApiKey -> "Khóa API không hợp lệ."
                            OnlineResult.Error.NotFound -> "Không tìm thấy thực phẩm phù hợp."
                            OnlineResult.Error.ServiceUnavailable -> "Dịch vụ đang tạm thời không khả dụng."
                            is OnlineResult.Error.Unknown -> result.message ?: "Không thể tra cứu thực phẩm."
                        }
                    )
                }
            }
        }
    }
}
