package com.annhien.womenshealth.feature.utilities

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesScreen(
    onNavigateToRoute: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tiện ích & Chuyên đề",
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
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Chuyên đề sức khỏe theo giai đoạn
            Text(
                text = "Chuyên đề sức khỏe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Chu kỳ kinh nguyệt",
                        subtitle = "Lịch sử chu kỳ, dự đoán và thống kê",
                        icon = Icons.Default.DateRange,
                        onClick = { onNavigateToRoute(Screen.CycleModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Kế hoạch mang thai",
                        subtitle = "BBT, que rụng trứng, ước tính thụ thai",
                        icon = Icons.Default.Favorite,
                        onClick = { onNavigateToRoute(Screen.ConceptionModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Theo dõi thai kỳ",
                        subtitle = "Tuần thai, đếm cử động, đồng hồ co thắt",
                        icon = Icons.Default.ChildCare,
                        onClick = { onNavigateToRoute(Screen.PregnancyModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Phục hồi sau sinh",
                        subtitle = "Sản dịch, cữ bú, tâm trạng và sàn chậu",
                        icon = Icons.Default.AccessibilityNew,
                        onClick = { onNavigateToRoute(Screen.PostpartumModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Tiền mãn kinh",
                        subtitle = "Cơn bốc hỏa, rối loạn kinh, thể chất",
                        icon = Icons.Default.WbSunny,
                        onClick = { onNavigateToRoute(Screen.PerimenopauseModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Vỉ thuốc tránh thai 28 viên",
                        subtitle = "Theo dõi ngày uống, xử trí khi quên thuốc",
                        icon = Icons.Default.MedicationLiquid,
                        onClick = { onNavigateToRoute(Screen.PillPackModule.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Chăm sóc hàng ngày & Thư giãn
            Text(
                text = "Chăm sóc & Thư giãn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Bài tập sàn chậu Kegel",
                        subtitle = "Đếm nhịp co thắt 5s - thả lỏng 5s chuẩn y khoa",
                        icon = Icons.Default.SelfImprovement,
                        onClick = { onNavigateToRoute(Screen.KegelModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Tự khám vú định kỳ",
                        subtitle = "4 bước tự kiểm tra mô tuyến vú tại nhà",
                        icon = Icons.Default.HealthAndSafety,
                        onClick = { onNavigateToRoute(Screen.BreastSelfExamModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Trắc nghiệm trầm cảm EPDS",
                        subtitle = "Sàng lọc tâm lý sau sinh 10 câu hỏi chuẩn",
                        icon = Icons.Default.Quiz,
                        onClick = { onNavigateToRoute(Screen.EpdsScreeningModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Sức khỏe mỗi ngày",
                        subtitle = "Theo dõi nước uống, giấc ngủ, cân nặng",
                        icon = Icons.Default.FitnessCenter,
                        onClick = { onNavigateToRoute(Screen.DailyHealthModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Thư giãn & Bài tập thở",
                        subtitle = "5 bài tập thở, Wim Hof, PMR & tĩnh tâm",
                        icon = Icons.Default.Air,
                        onClick = { onNavigateToRoute(Screen.RelaxationModule.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Sổ khám bệnh & Y bạ",
                        subtitle = "Lịch sử khám, kết quả xét nghiệm, thuốc",
                        icon = Icons.Default.FolderShared,
                        onClick = { onNavigateToRoute(Screen.MedicalRecordsModule.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Kiến thức, Cài đặt & Sao lưu
            Text(
                text = "Hệ thống & Dữ liệu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Thư viện kiến thức",
                        subtitle = "Cẩm nang y khoa & hướng dẫn ngoại tuyến",
                        icon = Icons.Default.MenuBook,
                        onClick = { onNavigateToRoute(Screen.Knowledge.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Trung tâm nhắc nhở",
                        subtitle = "Báo thức uống nước, nhật ký, thuốc, chu kỳ",
                        icon = Icons.Default.NotificationsActive,
                        onClick = { onNavigateToRoute(Screen.ReminderCenter.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Cài đặt & Bảo mật",
                        subtitle = "Khóa ứng dụng PIN, sinh trắc học, giao diện",
                        icon = Icons.Default.Settings,
                        onClick = { onNavigateToRoute(Screen.Settings.route) }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                    UtilityItemRow(
                        title = "Sao lưu & Khôi phục",
                        subtitle = "Xuất/nhập file sao lưu mã hóa ngoại tuyến",
                        icon = Icons.Default.Backup,
                        onClick = { onNavigateToRoute(Screen.BackupRestore.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun UtilityItemRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

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
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}
