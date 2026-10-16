package com.annhien.womenshealth.feature.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToJournalEdit: (LocalDate) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("'Tháng' M, yyyy", Locale("vi"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lịch sức khỏe",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            // Month Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước")
                }

                Text(
                    text = uiState.currentYearMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Weekday headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        days.forEach { day ->
                            Text(
                                text = day,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar month days
                    MonthGrid(
                        yearMonth = uiState.currentYearMonth,
                        selectedDate = uiState.selectedDate,
                        episodes = uiState.periodEpisodes,
                        predictedStart = uiState.predictedNextStart,
                        predictedEnd = uiState.predictedNextEnd,
                        hasJournalEntry = { date -> uiState.monthEntriesMap.containsKey(date) },
                        onDateClick = { date -> viewModel.selectDate(date) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = MaterialTheme.colorScheme.error, label = "Kỳ kinh")
                LegendItem(color = MaterialTheme.colorScheme.error.copy(alpha = 0.4f), label = "Dự đoán", isBorder = true)
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Nhật ký")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Day Details Sheet
            DayDetailsCard(
                selectedDate = uiState.selectedDate,
                entry = uiState.selectedDayJournalEntry,
                symptoms = uiState.selectedDaySymptoms,
                onLogJournal = { onNavigateToJournalEdit(uiState.selectedDate) },
                onTogglePeriod = { viewModel.togglePeriodOnSelectedDate() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    episodes: List<com.annhien.womenshealth.domain.model.PeriodEpisode>,
    predictedStart: LocalDate?,
    predictedEnd: LocalDate?,
    hasJournalEntry: (LocalDate) -> Boolean,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startDayOfWeek = (firstDayOfMonth.dayOfWeek.value - 1) // 0 for Monday

    val totalCells = ((startDayOfWeek + daysInMonth + 6) / 7) * 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col
                    val dayNumber = dayIndex - startDayOfWeek + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isPeriod = episodes.any { ep ->
                            val end = ep.endDate ?: LocalDate.now()
                            !date.isBefore(ep.startDate) && !date.isAfter(end)
                        }
                        val isPredicted = predictedStart != null && predictedEnd != null &&
                                !date.isBefore(predictedStart) && !date.isAfter(predictedEnd)
                        val hasJournal = hasJournalEntry(date)

                        DayCell(
                            date = date,
                            isSelected = isSelected,
                            isPeriod = isPeriod,
                            isPredicted = isPredicted,
                            hasJournal = hasJournal,
                            onClick = { onDateClick(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.size(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isPeriod: Boolean,
    isPredicted: Boolean,
    hasJournal: Boolean,
    onClick: () -> Unit
) {
    val isToday = date == LocalDate.now()

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                when {
                    isPeriod -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = when {
                    isToday -> 2.dp
                    isPredicted -> 1.5.dp
                    isSelected -> 1.5.dp
                    else -> 0.dp
                },
                color = when {
                    isToday -> MaterialTheme.colorScheme.primary
                    isPredicted -> MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isPeriod -> MaterialTheme.colorScheme.onError
                    isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            if (hasJournal && !isPeriod) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isBorder: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isBorder) Color.Transparent else color)
                .border(if (isBorder) 1.5.dp else 0.dp, color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DayDetailsCard(
    selectedDate: LocalDate,
    entry: com.annhien.womenshealth.domain.model.JournalEntry?,
    symptoms: List<com.annhien.womenshealth.domain.model.SymptomObservation>,
    onLogJournal: () -> Unit,
    onTogglePeriod: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M", Locale("vi"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.format(formatter),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onTogglePeriod) {
                    Text("Đánh dấu kinh nguyệt", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (entry != null || symptoms.isNotEmpty()) {
                if (symptoms.isNotEmpty()) {
                    Text(
                        text = "Triệu chứng đã ghi nhận:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptoms.forEach { obs ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(obs.symptomName) }
                            )
                        }
                    }
                }

                if (!entry?.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ghi chú: ${entry?.note}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Chưa có bản ghi nhật ký hay triệu chứng cho ngày này.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLogJournal,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (entry != null) "Chỉnh sửa nhật ký" else "Ghi nhật ký ngày này")
            }
        }
    }
}
