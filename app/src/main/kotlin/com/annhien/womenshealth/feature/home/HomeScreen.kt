package com.annhien.womenshealth.feature.home

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.domain.usecase.cycle.CyclePredictionResult
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToJournal: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToModule: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, 'ngày' d 'tháng' M", Locale("vi"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hôm nay",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.currentDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Lịch sức khỏe")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Cycle & Period Status Card
            CycleHeroCard(
                uiState = uiState,
                onTogglePeriod = { viewModel.togglePeriodToday() },
                onCardClick = onNavigateToCalendar
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Quick Log Action Row
            Text(
                text = "Ghi nhanh hôm nay",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    icon = Icons.Default.MedicalServices,
                    label = "Triệu chứng",
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToJournal
                )
                QuickActionButton(
                    icon = Icons.Default.SentimentSatisfied,
                    label = "Tâm trạng",
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateToJournal
                )
                QuickActionButton(
                    icon = Icons.Default.Bedtime,
                    label = "Giấc ngủ",
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    tint = MaterialTheme.colorScheme.tertiary,
                    onClick = onNavigateToJournal
                )
                QuickActionButton(
                    icon = Icons.Default.WaterDrop,
                    label = "+250ml Nước",
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tint = MaterialTheme.colorScheme.primary,
                    onClick = { viewModel.logQuickWater(250) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Hydration & Habits Progress
            HydrationCard(
                waterIntakeMl = uiState.todayWaterIntakeMl,
                targetMl = 2000,
                onAddWater = { viewModel.logQuickWater(250) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Daily Reassurance / Health Insight
            HealthTipCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CycleHeroCard(
    uiState: HomeUiState,
    onTogglePeriod: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val isPeriodOngoing = uiState.ongoingEpisode != null

            // Indicator Badge
            Surface(
                shape = CircleShape,
                color = if (isPeriodOngoing) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surface
            ) {
                Text(
                    text = if (isPeriodOngoing) "Đang trong kỳ kinh nguyệt"
                    else if (uiState.cycleDay != null) "Ngày ${uiState.cycleDay} của chu kỳ"
                    else "Chưa có kỳ kinh được ghi nhận",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPeriodOngoing) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Forecast or status text
            if (isPeriodOngoing) {
                Text(
                    text = "Hãy chú ý nghỉ ngơi và giữ ấm cơ thể",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else when (val pred = uiState.predictionResult) {
                is CyclePredictionResult.Predicted -> {
                    val days = uiState.daysUntilNextPeriod
                    Text(
                        text = if (days != null && days > 0) "Dự đoán kỳ kinh sau: khoảng $days ngày nữa"
                        else if (days != null && days == 0L) "Dự đoán kỳ kinh có thể bắt đầu hôm nay"
                        else "Kỳ kinh có thể đang chậm hơn dự kiến",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dựa trên trung vị các chu kỳ trước. Mang tính chất tham khảo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                is CyclePredictionResult.Ineligible -> {
                    Text(
                        text = "Theo dõi chu kỳ đều đặn",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pred.reasonVi,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                null -> {
                    Text(
                        text = "Bắt đầu ghi nhận kỳ kinh đầu tiên",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = onTogglePeriod,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPeriodOngoing) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (isPeriodOngoing) Icons.Default.Check else Icons.Default.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPeriodOngoing) "Đánh dấu kết thúc kỳ kinh" else "Kỳ kinh bắt đầu hôm nay"
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun HydrationCard(
    waterIntakeMl: Int,
    targetMl: Int,
    onAddWater: () -> Unit
) {
    val progress = (waterIntakeMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uống nước hôm nay",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "$waterIntakeMl / $targetMl ml",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
private fun HealthTipCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Lưu ý chăm sóc bản thân",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mỗi người phụ nữ có một nhịp sinh học và độ dài chu kỳ độc đáo. Việc lắng nghe và ghi nhận các dấu hiệu tự nhiên giúp bạn thấu hiểu cơ thể mình tốt hơn mỗi ngày.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
