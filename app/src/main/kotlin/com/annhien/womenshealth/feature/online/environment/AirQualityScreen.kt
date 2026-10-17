package com.annhien.womenshealth.feature.online.environment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQualityScreen(
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit,
    viewModel: AirQualityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chất lượng không khí") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val state = uiState) {
                AirQualityUiState.Idle -> {
                    Button(
                        onClick = {
                            viewModel.loadNearbyLocations(latitude, longitude)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tìm khu vực quan sát gần đây")
                    }
                }

                AirQualityUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text("Đang tải dữ liệu không khí…")
                    }
                }

                is AirQualityUiState.Success -> {
                    if (state.locations.isEmpty()) {
                        Text("Không tìm thấy khu vực quan sát không khí gần vị trí này.")
                    } else {
                        if (state.fromCache) {
                            Text(
                                text = "Dữ liệu từ bộ nhớ tạm",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.locations, key = { it.id ?: it.name ?: "" }) { location ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = location.name ?: "Khu vực quan sát",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        location.country?.name?.let {
                                            Text(
                                                text = "Quốc gia: $it",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        location.coordinates?.let {
                                            Text(
                                                text = "Tọa độ: ${it.latitude}, ${it.longitude}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        location.lastUpdated?.let {
                                            Text(
                                                text = "Cập nhật: $it",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                is AirQualityUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            viewModel.loadNearbyLocations(latitude, longitude)
                        }
                    ) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }
}
