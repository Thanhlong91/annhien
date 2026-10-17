package com.annhien.womenshealth.feature.cycle

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.PeriodEpisode
import com.annhien.womenshealth.domain.repository.CycleRepository
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionEngine
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

data class CycleModuleUiState(
    val episodes: List<PeriodEpisode> = emptyList(),
    val completedCycles: List<CycleSummary> = emptyList(),
    val prediction: CyclePredictionResult? = null,
    val showAddDialog: Boolean = false
)

@HiltViewModel
class CycleModuleViewModel @Inject constructor(
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleModuleUiState())
    val uiState: StateFlow<CycleModuleUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            cycleRepository.observePeriodEpisodes().collect { episodes ->
                val cycles = CyclePredictionEngine.buildCycleSummaries(episodes)
                val lastEpisode = episodes.sortedByDescending { it.startDate }.firstOrNull()
                val pred = if (lastEpisode != null) {
                    CyclePredictionEngine.predictNextPeriod(lastEpisode.startDate, cycles)
                } else null
                _uiState.update {
                    it.copy(
                        episodes = episodes.sortedByDescending { e -> e.startDate },
                        completedCycles = cycles,
                        prediction = pred
                    )
                }
            }
        }
    }

    fun addPeriod(startDate: LocalDate, endDate: LocalDate?) {
        viewModelScope.launch {
            val id = cycleRepository.recordPeriodStart(startDate, isConfirmed = true)
            if (endDate != null) {
                cycleRepository.recordPeriodEnd(id, endDate)
            }
            loadData()
        }
    }

    fun deletePeriod(id: String) {
        viewModelScope.launch {
            cycleRepository.deleteEpisode(id)
            loadData()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleModuleScreen(
    viewModel: CycleModuleViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("d 'tháng' M, yyyy", Locale("vi")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chu kỳ kinh nguyệt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Prediction Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dự đoán chu kỳ tiếp theo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        when (val pred = uiState.prediction) {
                            is CyclePredictionResult.Eligible -> {
                                val p = pred.prediction
                                Text(
                                    text = "Dự kiến: ${p.earliestEstimate.format(formatter)} – ${p.latestEstimate.format(formatter)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Độ dài chu kỳ trung vị: ${p.medianCycleLengthDays} ngày. Dao động: ${p.minCycleLengthDays} – ${p.maxCycleLengthDays} ngày.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            is CyclePredictionResult.Ineligible -> {
                                Text(
                                    text = pred.reason.messageVi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            null -> {
                                Text(
                                    text = "Chưa có dự đoán.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lưu ý: Dự đoán chỉ mang tính chất tham khảo dựa trên lịch sử ghi chép, không phải chẩn đoán y tế.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // History header
            item {
                Text(
                    text = "Lịch sử các kỳ kinh nguyệt",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (uiState.episodes.isEmpty()) {
                item {
                    Text(
                        text = "Chưa có kỳ kinh nào được ghi nhận.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(uiState.episodes) { episode ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bắt đầu: ${episode.startDate.format(formatter)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (episode.isOngoing) "Đang diễn ra"
                                    else "Kết thúc: ${episode.endDate?.format(formatter) ?: "—"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (episode.isOngoing) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(onClick = { viewModel.deletePeriod(episode.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
