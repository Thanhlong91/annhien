package com.annhien.womenshealth.feature.journal

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEditScreen(
    date: LocalDate,
    entryId: String? = null,
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val editState by viewModel.editState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(date, entryId) {
        viewModel.initEdit(date, entryId)
    }

    LaunchedEffect(editState.isSaved) {
        if (editState.isSaved) {
            onNavigateBack()
        }
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M, yyyy", Locale("vi"))
    }

    val commonSymptoms = listOf(
        "Đau bụng dưới", "Đau lưng", "Căng tức ngực", "Mệt mỏi",
        "Đau đầu", "Đầy hơi", "Nổi mụn", "Mất ngủ", "Thèm ngọt",
        "Buồn nôn", "Chóng mặt", "Thay đổi nhiệt độ"
    )

    val moodTagsList = listOf(
        "Bình yên", "Hạnh phúc", "Nhạy cảm", "Dễ cáu gắt",
        "Lo âu", "Mệt mỏi", "Tràn đầy năng lượng", "Buồn bã"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Ghi nhật ký",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = editState.selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveJournal() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu")
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
            Spacer(modifier = Modifier.height(12.dp))

            // 1. Symptoms selection
            Text(
                text = "Triệu chứng thể chất",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                commonSymptoms.forEach { symptom ->
                    val isSelected = editState.selectedSymptoms.contains(symptom)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleSymptom(symptom) },
                        label = { Text(symptom) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Mood selector
            Text(
                text = "Tâm trạng hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when (editState.selectedMoodLevel) {
                            1 -> "Rất khó chịu / Buồn"
                            2 -> "Hơi nhạy cảm / Lo lắng"
                            3 -> "Bình thường / Ổn định"
                            4 -> "Vui vẻ / Thoải mái"
                            5 -> "Rất tích cực / Tràn đầy năng lượng"
                            else -> "Bình thường"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = editState.selectedMoodLevel.toFloat(),
                        onValueChange = { viewModel.updateMoodLevel(it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodTagsList.forEach { tag ->
                            val isSelected = editState.selectedMoodTags.contains(tag)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleMoodTag(tag) },
                                label = { Text(tag) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Sleep tracker
            Text(
                text = "Giấc ngủ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thời gian ngủ: ${String.format(Locale.US, "%.1f", editState.sleepHours)} giờ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Slider(
                        value = editState.sleepHours,
                        onValueChange = { /* state handled in viewmodel if needed */ },
                        valueRange = 3f..12f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Notes
            Text(
                text = "Ghi chú riêng tư",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = editState.note,
                onValueChange = { viewModel.updateNote(it) },
                placeholder = { Text("Viết đôi dòng chia sẻ với chính mình...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
