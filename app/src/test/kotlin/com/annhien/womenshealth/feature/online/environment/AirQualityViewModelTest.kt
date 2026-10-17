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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val sampleLocation = OpenAqLocation(
    id = 1,
    name = "Sample Station",
    country = null,
    coordinates = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class AirQualityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: OpenAqRepository
    private lateinit var locationProvider: LocationProvider
    private lateinit var viewModel: AirQualityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        locationProvider = mockk()
        viewModel = AirQualityViewModel(
            repository = repository,
            locationProvider = locationProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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
            assertEquals(AirQualityUiState.Idle, awaitItem())

            viewModel.loadNearbyLocations(21.0, 105.0)
            testScheduler.advanceUntilIdle()

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
            assertEquals(AirQualityUiState.Idle, awaitItem())

            viewModel.loadNearbyLocations(21.0, 105.0)
            testScheduler.advanceUntilIdle()

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val error = awaitItem() as AirQualityUiState.Error
            assertEquals(
                "Không có kết nối Internet. Vui lòng kiểm tra mạng và thử lại.",
                error.message
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNearbyLocationsUsingDeviceLocation emits error when permission missing`() = runTest {
        every { locationProvider.hasLocationPermission() } returns false

        viewModel.uiState.test {
            assertEquals(AirQualityUiState.Idle, awaitItem())

            viewModel.loadNearbyLocationsUsingDeviceLocation()
            testScheduler.advanceUntilIdle()

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
            assertEquals(AirQualityUiState.Idle, awaitItem())

            viewModel.loadNearbyLocationsUsingDeviceLocation()
            testScheduler.advanceUntilIdle()

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
        } returns flow {
            throw SecurityException("Location permission is not granted")
        }

        viewModel.uiState.test {
            assertEquals(AirQualityUiState.Idle, awaitItem())

            viewModel.loadNearbyLocationsUsingDeviceLocation()
            testScheduler.advanceUntilIdle()

            assertEquals(AirQualityUiState.Loading, awaitItem())

            val error = awaitItem() as AirQualityUiState.Error
            assertTrue(error.message.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
