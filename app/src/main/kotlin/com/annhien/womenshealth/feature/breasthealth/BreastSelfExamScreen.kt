package com.annhien.womenshealth.feature.breasthealth

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreastSelfExamScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var findingsNoted by remember { mutableStateOf(false) }
    var notesInput by remember { mutableStateOf("") }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tự khám vú định kỳ tại nhà", fontWeight = FontWeight.Bold) },
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

            // Banner Thời Điểm Vàng
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Thời điểm vàng: 3–5 ngày sau khi sạch kinh",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lúc này nồng độ Estrogen giảm thấp, mô tuyến vú mềm mại nhất, không bị cương đau sinh lý, giúp phát hiện bất thường dễ dàng nhất.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "4 bước tự kiểm tra theo chuẩn y khoa",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Bước 1
            ExamStepCard(
                stepNumber = "1",
                title = "Quan sát trước gương ở tư thế đứng",
                description = "Cởi áo, đứng thẳng xuôi hai tay. Sau đó chống hai tay lên hông, ưỡn ngực ra trước. Quan sát sự cân đối hai bên ngực, xem có vết lõm da, sần da cam hay đổi màu bất thường không.",
                icon = Icons.Default.Visibility
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bước 2
            ExamStepCard(
                stepNumber = "2",
                title = "Giơ hai tay lên cao qua đầu",
                description = "Quan sát xem bờ cong dưới của bầu ngực có dịch chuyển cân đối hay không. Kiểm tra xem núm vú có bị kéo tụt vào trong hoặc co kéo lệch hướng.",
                icon = Icons.Default.Accessibility
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bước 3
            ExamStepCard(
                stepNumber = "3",
                title = "Sờ nắn ở tư thế nằm ngửa",
                description = "Đặt gối mỏng dưới vai trái, tay trái đưa ra sau gáy. Dùng 3 ngón tay giữa của bàn tay phải khép lại, day nhẹ nhàng theo chuyển động tròn xoắn ốc từ ngoài rìa vào núm vú để cảm nhận mô bên trong. Đổi bên lặp lại.",
                icon = Icons.Default.TouchApp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bước 4
            ExamStepCard(
                stepNumber = "4",
                title = "Kiểm tra núm vú & Hõm nách",
                description = "Dùng ngón cái và ngón trỏ bóp nhẹ vào chân núm vú xem có tiết dịch vàng đục hoặc dịch máu không. Dùng các đầu ngón tay rà soát kỹ vùng hõm nách hai bên để kiểm tra hạch phì đại.",
                icon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Ghi nhận kết quả hôm nay
            Text(
                text = "Ghi nhận kết quả tự khám hôm nay",
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !findingsNoted,
                            onClick = { findingsNoted = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mô vú mềm mại bình thường, không có khối u",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = findingsNoted,
                            onClick = { findingsNoted = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Có khối cộm / đau / tiết dịch bất thường",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (findingsNoted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (findingsNoted) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = notesInput,
                            onValueChange = { notesInput = it },
                            label = { Text("Mô tả vị trí khối cộm hoặc dấu hiệu bất thường") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Lưu ý: Hầu hết các nốt sần là u xơ lành tính hoặc nang tuyến vú. Tuy nhiên, bạn nên sắp xếp đi khám chuyên khoa phụ sản/nhũ hoa trong vòng 1 tuần để được siêu âm chẩn đoán chính xác.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            saveSuccessMessage = if (!findingsNoted) {
                                "Đã lưu kết quả tự khám vú (Bình thường). Lần tự khám tiếp theo: 1 tháng nữa."
                            } else {
                                "Đã lưu ghi nhận bất thường vào sổ y bạ ngoại tuyến. Hãy đặt lịch khám sớm."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lưu kết quả tự khám")
                    }

                    if (saveSuccessMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = saveSuccessMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ExamStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
