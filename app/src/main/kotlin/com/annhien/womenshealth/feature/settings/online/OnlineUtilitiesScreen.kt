package com.annhien.womenshealth.feature.settings.online

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhien.womenshealth.core.ratelimit.RateLimitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineUtilitiesScreen(
    viewModel: OnlineUtilitiesViewModel = hiltViewModel()
) {
    val utilities by viewModel.utilities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Tiện ích trực tuyến") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(utilities, key = { it.sourceId }) { utility ->
                OnlineUtilityCard(
                    utility = utility,
                    onToggle = { enabled ->
                        viewModel.setEnabled(utility.sourceId, enabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun OnlineUtilityCard(
    utility: OnlineUtilityUiState,
    onToggle: (Boolean) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = utility.titleVi, style = MaterialTheme.typography.titleMedium)
                    Text(text = utility.descriptionVi, style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = utility.isEnabled,
                    onCheckedChange = onToggle
                )
            }

            val stateText = when {
                !utility.isEnabled -> "Đã tắt"
                utility.status.state == RateLimitState.LIMITED -> "Đã đạt giới hạn"
                utility.status.state == RateLimitState.DISABLED -> "Không khả dụng"
                else -> "Khả dụng"
            }

            Text(
                text = stateText,
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    !utility.isEnabled -> MaterialTheme.colorScheme.outline
                    utility.status.state == RateLimitState.LIMITED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
            )

            utility.status.message?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
