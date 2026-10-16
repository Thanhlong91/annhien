package com.annhien.womenshealth.feature.familyplanning

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillPackScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // 28 viên thuốc (1..28) -> boolean đã uống
    val takenPills = remember {
        mutableStateMapOf<Int, Boolean>().apply {
            // Mặc định đánh dấu các ngày trước đó đã uống mẫu (ví dụ ngày 1..13)
            for (i in 1..13) put(i, true)
            for (i in 14..28) put(i, false)
        }
    }

    val totalActive = 21
    val totalPlacebo = 7
    val takenCount = takenPills.values.count { it }
    val currentDay = (takenCount + 1).coerceAtMost(28)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vỉ thuốc tránh thai 28 viên", fontWeight = FontWeight.Bold) },
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

            // Thẻ trạng thái vỉ thuốc
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Ngày $currentDay của vỉ thuốc",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentDay <= 21) "Viên hoạt tính (Ngừa thai)" else "Viên nhắc giả dược (Tuần có kinh)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "$takenCount / 28 viên",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vỉ thuốc mô phỏng (4 hàng x 7 cột)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chạm vào viên thuốc để đánh dấu đã uống:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lưới 4 tuần x 7 viên
                    for (row in 0..3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (col in 0..6) {
                                val pillIndex = row * 7 + col + 1
                                val isPlacebo = pillIndex > 21
                                val isTaken = takenPills[pillIndex] == true

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isTaken -> MaterialTheme.colorScheme.primary
                                                isPlacebo -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                            }
                                        )
                                        .border(
                                            width = if (pillIndex == currentDay && !isTaken) 2.dp else 1.dp,
                                            color = if (pillIndex == currentDay && !isTaken) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            takenPills[pillIndex] = !isTaken
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (isTaken) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Đã uống",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$pillIndex",
                                                fontSize = 13.sp,
                                                fontWeight = if (pillIndex == currentDay) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = if (isPlacebo) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Chú thích màu sắc
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Viên hoạt tính (1–21)", style = MaterialTheme.typography.labelSmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Viên giả dược (22–28)", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cẩm nang xử trí khi quên uống thuốc
            Text(
                text = "Hướng dẫn xử trí khi quên uống thuốc",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Quên 1 viên hoạt tính (< 12 giờ):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uống ngay viên thuốc bị quên ngay khi nhớ ra (kể cả phải uống 2 viên trong cùng 1 ngày). Uống viên tiếp theo đúng giờ như thường lệ. Bạn vẫn được bảo vệ tránh thai an toàn.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = "2. Quên > 12 giờ hoặc quên từ 2 viên trở lên:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Uống ngay viên gần nhất, bỏ viên trước đó. Tiếp tục uống các viên tiếp theo đúng giờ. BẮT BUỘC sử dụng thêm biện pháp tránh thai dự phòng (như bao cao su) trong 7 ngày kế tiếp.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.surfaceVariant)

                    Text(
                        text = "3. Quên uống viên giả dược (viên 22–28):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Bỏ viên giả dược bị quên và tiếp tục uống bình thường. Viên giả dược không chứa hormone nên không làm giảm hiệu quả tránh thai.",
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
