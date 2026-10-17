package com.annhien.womenshealth.core.location

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LocationProviderTest {

    private lateinit var context: Context
    private lateinit var locationProvider: LocationProvider

    @Before
    fun setUp() {
        context = mockk()
        locationProvider = LocationProvider(context)

        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `hasLocationPermission returns true when fine location granted`() {
        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_GRANTED

        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        assertTrue(locationProvider.hasLocationPermission())
    }

    @Test
    fun `hasLocationPermission returns true when coarse location granted`() {
        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } returns PackageManager.PERMISSION_GRANTED

        assertTrue(locationProvider.hasLocationPermission())
    }

    @Test
    fun `hasLocationPermission returns false when both permissions denied`() {
        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        assertFalse(locationProvider.hasLocationPermission())
    }

    @Test
    fun `currentLocation closes with security exception when permission missing`() = runTest {
        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        every {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } returns PackageManager.PERMISSION_DENIED

        val result = runCatching {
            locationProvider.currentLocation().first()
        }

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
