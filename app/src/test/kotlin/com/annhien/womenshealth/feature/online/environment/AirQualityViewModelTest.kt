package com.annhien.womenshealth.feature.online.environment

import app.cash.turbine.test
import com.annhien.womenshealth.core.connectivity.OnlineResult
import com.annhien.womenshealth.core.location.LocationData
import com.annhien.womenshealth.core.location.LocationProvider
import com.annhien.womenshealth.core.online.environment.OpenAqLocation
import com.annhien.womenshealth.core.online.environment.OpenAqRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val sampleLocation = OpenAqLocation(
    id = "location-1",
    name = "Sample Station",
    country = null,
    coordinates = null,
    lastUpdated = null
)

class AirQualityViewModelTest {

    private lateinit var repository: OpenAqRepository
    private lateinit var locationProvider: LocationProvider
    private lateinit var viewModel: AirQualityViewModel

    @Before
    fun setUp() {
        repository = mockk()
        locationProvider = mockk()
        viewModel = AirQualityViewModel(
            repository = repository,
            locationProvider = locationProvider
        )
    }

    @Test
    fun `loadNearbyLocations emits loading then success`() = runTest {
        coEvery {
            repository.getNearbyLocations(21.0, 105.0)
        } returns OnlineResult.Success(
            data = listOf(sampleLocation),
            fromCache = false
        )

        viewModel.uiState.test {
            viewModel.loadNearbyLocations(21.0, 105.0)

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val success = awaitItem() as AirQualityUiState.Success
            assertEquals(listOf(sampleLocation), success.locations)
            assertEquals(false, success.fromCache)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNearbyLocations emits error when repository fails offline`() = runTest {
        coEvery {
            repository.getNearbyLocations(21.0, 105.0)
        } returns OnlineResult.Error.Offline

        viewModel.uiState.test {
            viewModel.loadNearbyLocations(21.0, 105.0)

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val error = awaitItem() as AirQualityUiState.Error
            assertEquals(
                "Không có kết nối Internet. Vui lòng thử lại khi có mạng.",
                error.message
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNearbyLocationsUsingDeviceLocation emits error when permission missing`() = runTest {
        every { locationProvider.hasLocationPermission() } returns false

        viewModel.uiState.test {
            viewModel.loadNearbyLocationsUsingDeviceLocation()

            val error = awaitItem() as AirQualityUiState.Error
            assertEquals(
                "Cần quyền vị trí để tìm khu vực quan sát gần bạn.",
                error.message
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNearbyLocationsUsingDeviceLocation loads repository using device location`() = runTest {
        every { locationProvider.hasLocationPermission() } returns true
        every { locationProvider.currentLocation() } returns flowOf(
            LocationData(latitude = 21.0, longitude = 105.0)
        )

        coEvery {
            repository.getNearbyLocations(21.0, 105.0)
        } returns OnlineResult.Success(
            data = listOf(sampleLocation),
            fromCache = false
        )

        viewModel.uiState.test {
            viewModel.loadNearbyLocationsUsingDeviceLocation()

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val success = awaitItem() as AirQualityUiState.Success
            assertEquals(listOf(sampleLocation), success.locations)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNearbyLocationsUsingDeviceLocation emits error when location stream fails`() = runTest {
        every { locationProvider.hasLocationPermission() } returns true
        every {
            locationProvider.currentLocation()
        } throws SecurityException("Location permission is not granted")

        viewModel.uiState.test {
            viewModel.loadNearbyLocationsUsingDeviceLocation()

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val error = awaitItem() as AirQualityUiState.Error
            assertTrue(error.message.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
