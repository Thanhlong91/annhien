package com.annhien.womenshealth.feature.reminders

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ReminderItemState(
    val id: String,
    val title: String,
    val description: String,
    val defaultTime: String,
    val icon: ImageVector,
    var isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCenterScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var periodReminder by remember { mutableStateOf(true) }
    var waterReminder by remember { mutableStateOf(true) }
    var journalReminder by remember { mutableStateOf(true) }
    var medicationReminder by remember { mutableStateOf(true) }
    var kegelReminder by remember { mutableStateOf(false) }

    var saveToast by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trung tâm nhắc nhở", fontWeight = FontWeight.Bold) },
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

            // Banner giải thích bảo mật Zero-Internet
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Nhắc nhở hoàn toàn ngoại tuyến",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Báo thức chạy trực tiếp bằng AlarmManager của Android. Không có thông báo đẩy qua máy chủ, đảm bảo riêng tư 100%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Lịch nhắc sức khỏe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ReminderSwitchRow(
                        title = "Dự báo chu kỳ kinh",
                        subtitle = "Báo trước 2 ngày khi sắp đến kỳ kinh dự kiến",
                        time = "08:00 sáng",
                        icon = Icons.Default.DateRange,
                        checked = periodReminder,
                        onCheckedChange = {
                            periodReminder = it
                            saveToast = if (it) "Đã bật nhắc chu kỳ" else "Đã tắt nhắc chu kỳ"
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ReminderSwitchRow(
                        title = "Uống nước đủ lượng",
                        subtitle = "Nhắc nhở uống một cốc nước mỗi 2 tiếng",
                        time = "08:00 - 20:00",
                        icon = Icons.Default.WaterDrop,
                        checked = waterReminder,
                        onCheckedChange = {
                            waterReminder = it
                            saveToast = if (it) "Đã bật nhắc uống nước" else "Đã tắt nhắc uống nước"
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ReminderSwitchRow(
                        title = "Ghi nhật ký cảm xúc",
                        subtitle = "Dành 1 phút ghi nhận thể trạng & cảm xúc trong ngày",
                        time = "21:00 tối",
                        icon = Icons.Default.EditNote,
                        checked = journalReminder,
                        onCheckedChange = {
                            journalReminder = it
                            saveToast = if (it) "Đã bật nhắc nhật ký" else "Đã tắt nhắc nhật ký"
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ReminderSwitchRow(
                        title = "Uống thuốc & Vi chất",
                        subtitle = "Báo thức theo giờ uống thuốc/vi chất đã lưu",
                        time = "Theo đơn thuốc",
                        icon = Icons.Default.Medication,
                        checked = medicationReminder,
                        onCheckedChange = {
                            medicationReminder = it
                            saveToast = if (it) "Đã bật chuông thuốc" else "Đã tắt chuông thuốc"
                        }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))

                    ReminderSwitchRow(
                        title = "Bài tập Kegel & Thư giãn",
                        subtitle = "Nhắc 10 nhịp co thả sàn chậu phục hồi thể lực",
                        time = "15:00 chiều",
                        icon = Icons.Default.SelfImprovement,
                        checked = kegelReminder,
                        onCheckedChange = {
                            kegelReminder = it
                            saveToast = if (it) "Đã bật nhắc Kegel" else "Đã tắt nhắc Kegel"
                        }
                    )
                }
            }

            if (saveToast != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveToast!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ReminderSwitchRow(
    title: String,
    subtitle: String,
    time: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Thời gian: $time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
