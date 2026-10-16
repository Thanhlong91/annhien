package com.annhien.womenshealth.feature.postpartum

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostpartumModuleScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedLochiaColor by remember { mutableStateOf("Hồng / Nâu nhạt (Lochia serosa)") }
    var selectedSide by remember { mutableStateOf("Vú trái") }
    var feedingMinutes by remember { mutableStateOf(15) }
    var saveStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phục hồi sau sinh", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
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
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Lochia Tracking Card
            Text(
                text = "Theo dõi sản dịch (Lochia)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Màu sắc sản dịch bình thường sẽ nhạt dần từ đỏ tươi sang nâu, vàng nhạt rồi chuyển sang trong suốt sau vài tuần.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val options = listOf(
                        "Đỏ tươi (Lochia rubra - tuần đầu)",
                        "Hồng / Nâu nhạt (Lochia serosa)",
                        "Trắng đục / Vàng nhạt (Lochia alba)",
                        "Có mùi hôi bất thường (Cần đi khám ngay)"
                    )

                    options.forEach { opt ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLochiaColor == opt,
                                onClick = { selectedLochiaColor = opt }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = opt, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Feeding Tracker
            Text(
                text = "Ghi cữ cho bé bú",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("Vú trái", "Vú phải", "Bình sữa").forEach { side ->
                            FilterChip(
                                selected = selectedSide == side,
                                onClick = { selectedSide = side },
                                label = { Text(side) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Thời lượng bú: $feedingMinutes phút",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Slider(
                        value = feedingMinutes.toFloat(),
                        onValueChange = { feedingMinutes = it.toInt() },
                        valueRange = 5f..45f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            saveStatus = "Đã lưu cữ bú $selectedSide ($feedingMinutes phút) vào nhật ký ngoại tuyến."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Lưu cữ bú")
                    }

                    if (saveStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = saveStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Phục hồi sàn chậu & Bài tập Kegel
            KegelTrainerCard(
                onSessionCompleted = { reps ->
                    saveStatus = "Đã hoàn thành hiệp tập Kegel ($reps nhịp) phục hồi cơ sàn chậu."
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Postpartum Mental Health Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Chăm sóc tinh thần mẹ sau sinh",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Hiện tượng 'Baby Blues' là phản ứng sinh lý thường gặp sau khi sinh do suy giảm hormone đột ngột. Nếu bạn cảm thấy kiệt sức, lo âu kéo dài trên 2 tuần hoặc có suy nghĩ tiêu cực, hãy chia sẻ ngay với người thân hoặc liên hệ bác sĩ để được hỗ trợ kịp thời.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
