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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhien.womenshealth.core.ratelimit.RateLimitState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineUtilitiesScreen(
    viewModel: OnlineUtilitiesViewModel = hiltViewModel()
) {
    val statuses by viewModel.statuses.collectAsState()

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
            items(OnlineUtilityCatalog.utilities) { utility ->
                val status = statuses[utility.sourceId]
                OnlineUtilityCard(
                    title = utility.titleVi,
                    description = utility.descriptionVi,
                    state = status?.state ?: RateLimitState.AVAILABLE,
                    message = status?.message
                )
            }
        }
    }
}

@Composable
private fun OnlineUtilityCard(
    title: String,
    description: String,
    state: RateLimitState,
    message: String?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodyMedium)

            val stateText = when (state) {
                RateLimitState.AVAILABLE -> "Khả dụng"
                RateLimitState.LIMITED -> "Đã đạt giới hạn"
                RateLimitState.DISABLED -> "Không khả dụng"
            }

            Row {
                Text(
                    text = stateText,
                    style = MaterialTheme.typography.labelLarge,
                    color = when (state) {
                        RateLimitState.AVAILABLE -> MaterialTheme.colorScheme.primary
                        RateLimitState.LIMITED -> MaterialTheme.colorScheme.error
                        RateLimitState.DISABLED -> MaterialTheme.colorScheme.outline
                    }
                )
            }

            message?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
