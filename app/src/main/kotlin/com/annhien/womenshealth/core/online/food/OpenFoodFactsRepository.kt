package com.annhien.womenshealth.core.online.food

import com.annhien.womenshealth.core.connectivity.OnlineGateway
import com.annhien.womenshealth.core.connectivity.OnlineResult
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenFoodFactsRepository @Inject constructor(
    private val api: OpenFoodFactsApi,
    private val gateway: OnlineGateway
) {

    companion object {
        const val SOURCE_ID = "openfoodfacts"
        private const val CACHE_TTL_HOURS = 24L
    }

    suspend fun searchFood(query: String): OnlineResult<List<OpenFoodFactsProduct>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return OnlineResult.Error.Unknown("Vui lòng nhập tên thực phẩm cần tìm")
        }

        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "openfoodfacts_search_$trimmed",
            ttl = Duration.ofHours(CACHE_TTL_HOURS)
        ) {
            api.searchProducts(query = trimmed).products
        }
    }

    suspend fun getProductByBarcode(barcode: String): OnlineResult<OpenFoodFactsProduct?> {
        val trimmed = barcode.trim()
        if (trimmed.isEmpty()) {
            return OnlineResult.Error.Unknown("Mã vạch không hợp lệ")
        }

        return gateway.execute(
            sourceId = SOURCE_ID,
            cacheKey = "openfoodfacts_barcode_$trimmed",
            ttl = Duration.ofHours(CACHE_TTL_HOURS)
        ) {
            api.getProductByBarcode(barcode = trimmed).product
        }
    }
}
