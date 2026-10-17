package com.annhien.womenshealth.feature.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

    var showRangeDialog by remember { mutableStateOf(false) }
    var selectedDurationDays by remember { mutableIntStateOf(5) }

    val monthFormatter = remember {
        DateTimeFormatter.ofPattern("'Tháng' M, yyyy", Locale("vi"))
    }

    val isPeriodSelected = uiState.periodEpisodes.any { ep ->
        val end = ep.endDate ?: LocalDate.now()
        !uiState.selectedDate.isBefore(ep.startDate) && !uiState.selectedDate.isAfter(end)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lịch sức khỏe",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
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
            // Month Selector Bar (Matching Simulator)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousMonth() }) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Tháng trước", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text(
                    text = uiState.currentYearMonth.format(monthFormatter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { viewModel.nextMonth() }) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(36.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                modifier = Modifier.width(38.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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

            Spacer(modifier = Modifier.height(14.dp))

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = MaterialTheme.colorScheme.error, label = "Kỳ kinh")
                LegendItem(color = MaterialTheme.colorScheme.errorContainer, label = "Dự đoán", isBorder = true)
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Nhật ký")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Day Details Sheet with Easy Period Marking
            DayDetailsCard(
                selectedDate = uiState.selectedDate,
                isPeriod = isPeriodSelected,
                entry = uiState.selectedDayJournalEntry,
                symptoms = uiState.selectedDaySymptoms,
                onLogJournal = { onNavigateToJournalEdit(uiState.selectedDate) },
                onTogglePeriod = { viewModel.togglePeriodOnSelectedDate() },
                onOpenRangeDialog = { showRangeDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Range Dialog for recording e.g. 3-7 day period
    if (showRangeDialog) {
        val endDate = uiState.selectedDate.plusDays((selectedDurationDays - 1).toLong())
        val dateFormatter = remember { DateTimeFormatter.ofPattern("d/M/yyyy") }

        AlertDialog(
            onDismissRequest = { showRangeDialog = false },
            title = {
                Text(
                    text = "Ghi nhận cả kỳ kinh",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column {
                    Text(
                        text = "Bắt đầu từ: ${uiState.selectedDate.format(dateFormatter)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kéo dài bao nhiêu ngày?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(3, 4, 5, 6, 7).forEach { days ->
                            FilterChip(
                                selected = selectedDurationDays == days,
                                onClick = { selectedDurationDays = days },
                                label = { Text("${days}d") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Kỳ kinh: ${uiState.selectedDate.format(dateFormatter)}  👉  ${endDate.format(dateFormatter)} ($selectedDurationDays ngày)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordPeriodRange(uiState.selectedDate, endDate)
                        showRangeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Lưu kỳ kinh")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangeDialog = false }) {
                    Text("Hủy")
                }
            }
        )
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

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    isPredicted -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = when {
                    isSelected -> 2.dp
                    isToday -> 2.dp
                    isPredicted -> 1.5.dp
                    else -> 0.dp
                },
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    isPredicted -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
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
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday || isPeriod) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isPeriod -> Color.White
                    isPredicted -> MaterialTheme.colorScheme.error
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
                .size(12.dp)
                .clip(CircleShape)
                .background(if (isBorder) Color.Transparent else color)
                .border(if (isBorder) 2.dp else 0.dp, color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DayDetailsCard(
    selectedDate: LocalDate,
    isPeriod: Boolean,
    entry: com.annhien.womenshealth.domain.model.JournalEntry?,
    symptoms: List<com.annhien.womenshealth.domain.model.SymptomObservation>,
    onLogJournal: () -> Unit,
    onTogglePeriod: () -> Unit,
    onOpenRangeDialog: () -> Unit
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M, yyyy", Locale("vi"))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with date & status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedDate.format(formatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isPeriod) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (isPeriod) "🌸 Ngày có kinh" else "⚪ Không có kinh",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPeriod) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Prominent Action Buttons for Period Marking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTogglePeriod,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPeriod) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isPeriod) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp)
                ) {
                    Icon(
                        imageVector = if (isPeriod) Icons.Default.Close else Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPeriod) "Hủy ngày kinh" else "Đánh dấu có kinh",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onOpenRangeDialog,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(46.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đặt nhiều ngày",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Symptoms / Notes info
            if (entry != null || symptoms.isNotEmpty()) {
                if (symptoms.isNotEmpty()) {
                    Text(
                        text = "Triệu chứng ghi nhận:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptoms.forEach { obs ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(obs.symptomName, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                if (!entry?.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ghi chú: \"${entry?.note}\"",
                        style = MaterialTheme.typography.bodySmall,
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

            Spacer(modifier = Modifier.height(14.dp))

            // Journal button
            Button(
                onClick = onLogJournal,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (entry != null) "Chỉnh sửa nhật ký" else "Ghi nhật ký ngày này", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
