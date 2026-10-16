package com.annhien.womenshealth.feature.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annhien.womenshealth.core.reports.ReportExporter
import com.annhien.womenshealth.domain.model.CycleSummary
import com.annhien.womenshealth.domain.model.JournalEntry
import com.annhien.womenshealth.domain.model.PeriodEpisode
import com.annhien.womenshealth.domain.repository.CycleRepository
import com.annhien.womenshealth.domain.repository.JournalRepository
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

data class ReportsUiState(
    val episodes: List<PeriodEpisode> = emptyList(),
    val cycles: List<CycleSummary> = emptyList(),
    val averageCycleLength: Double? = null,
    val averagePeriodLength: Double? = null,
    val isRegular: Boolean? = null,
    val exportSuccessMessage: String? = null,
    val exportedFilePath: String? = null,
    val isExporting: Boolean = false
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cycleRepository: CycleRepository,
    private val journalRepository: JournalRepository,
    private val reportExporter: ReportExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportStats()
    }

    fun loadReportStats() {
        viewModelScope.launch {
            val episodes = cycleRepository.getRecentEpisodes(24)
            val cycles = CyclePredictionEngine.extractCompletedCycles(episodes)

            var avgCycle: Double? = null
            var avgPeriod: Double? = null
            var regular: Boolean? = null

            if (cycles.isNotEmpty()) {
                avgCycle = cycles.map { it.cycleLength }.average()
                avgPeriod = cycles.map { it.periodLength }.average()
                val minLen = cycles.minOf { it.cycleLength }
                val maxLen = cycles.maxOf { it.cycleLength }
                regular = (maxLen - minLen) <= 7
            }

            _uiState.update {
                it.copy(
                    episodes = episodes,
                    cycles = cycles,
                    averageCycleLength = avgCycle,
                    averagePeriodLength = avgPeriod,
                    isRegular = regular
                )
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null) }
            val now = LocalDate.now()
            val entries = journalRepository.searchNotes("") // fetch all notes
            val file = File(context.cacheDir, "Bao_Cao_Suc_Khoe_An_Nhien_${now}.pdf")

            val result = reportExporter.exportHealthReportPdf(
                episodes = _uiState.value.episodes,
                cycles = _uiState.value.cycles,
                entries = entries,
                outputFile = file
            )

            result.fold(
                onSuccess = { createdFile ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccessMessage = "Đã xuất tệp PDF thành công: ${createdFile.name}",
                            exportedFilePath = createdFile.absolutePath
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccessMessage = "Lỗi xuất báo cáo: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null) }
            val now = LocalDate.now()
            val file = File(context.cacheDir, "Chu_Ky_An_Nhien_${now}.csv")

            val result = reportExporter.exportCyclesCsv(
                episodes = _uiState.value.episodes,
                outputFile = file
            )

            result.fold(
                onSuccess = { createdFile ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccessMessage = "Đã xuất tệp CSV thành công: ${createdFile.name}",
                            exportedFilePath = createdFile.absolutePath
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportSuccessMessage = "Lỗi xuất dữ liệu: ${err.localizedMessage}"
                        )
                    }
                }
            )
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportSuccessMessage = null) }
    }
}
