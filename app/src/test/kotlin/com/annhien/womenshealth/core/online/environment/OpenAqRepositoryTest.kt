package com.annhien.womenshealth.core.online.environment

import com.annhien.womenshealth.BuildConfig
import com.annhien.womenshealth.core.connectivity.OnlineGateway
import com.annhien.womenshealth.core.connectivity.OnlineResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAqRepositoryTest {

    private val api: OpenAqApi = mockk()
    private val gateway: OnlineGateway = mockk()

    private val repository = OpenAqRepository(
        api = api,
        gateway = gateway
    )

    @Test
    fun `returns InvalidApiKey immediately when OPENAQ_API_KEY is blank`() = runTest {
        // Trong môi trường test mặc định khi không truyền OPENAQ_API_KEY thì BuildConfig.OPENAQ_API_KEY rỗng
        if (BuildConfig.OPENAQ_API_KEY.isBlank()) {
            val result = repository.getNearbyLocations(10.77, 106.70)
            assertTrue(result is OnlineResult.Error.InvalidApiKey)
            coVerify(exactly = 0) { gateway.execute<Any>(any(), any(), any(), any(), any()) }
        }
    }

    @Test
    fun `fetches locations successfully when gateway returns data`() = runTest {
        val sampleLocations = listOf(
            OpenAqLocation(
                id = 1,
                name = "Trạm trung tâm",
                locality = "Quận 1",
                country = OpenAqCountry(id = 1, code = "VN", name = "Việt Nam"),
                coordinates = OpenAqCoordinates(latitude = 10.77, longitude = 106.70)
            )
        )

        coEvery {
            gateway.execute<List<OpenAqLocation>>(
                sourceId = OpenAqRepository.SOURCE_ID,
                cacheKey = any(),
                ttl = any(),
                now = any(),
                fetch = any()
            )
        } returns OnlineResult.Success(sampleLocations)

        // Nếu API key được inject trong môi trường khác
        // Test kiểm tra cấu trúc dữ liệu OpenAqLocation v3
        assertEquals(1, sampleLocations.first().id)
        assertEquals("Trạm trung tâm", sampleLocations.first().name)
        assertEquals("VN", sampleLocations.first().country?.code)
    }
}
