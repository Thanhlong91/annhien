package com.annhien.womenshealth.feature.utilities

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.core.design.UtilityColorCategory
import com.annhien.womenshealth.core.design.getUtilityCategoryColors
import com.annhien.womenshealth.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilitiesScreen(
    onNavigateToRoute: (String) -> Unit,
    onQuickDisguise: (() -> Unit)? = null,
    onToggleTheme: (() -> Unit)? = null,
    isDarkTheme: Boolean = false
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
                actions = {
                    if (onToggleTheme != null) {
                        IconButton(onClick = onToggleTheme) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Chuyển giao diện sáng/tối",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    if (onQuickDisguise != null) {
                        IconButton(onClick = onQuickDisguise) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = "Chế độ ngụy trang máy tính",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
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

            // 1. Chuyên đề sức khỏe theo giai đoạn
            Text(
                text = "Chuyên đề sức khỏe",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Chu kỳ kinh nguyệt",
                        subtitle = "Lịch sử các kỳ kinh và dự đoán trung vị",
                        icon = Icons.Default.DateRange,
                        colorCategory = UtilityColorCategory.SAGE,
                        onClick = { onNavigateToRoute(Screen.CycleModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Kế hoạch mang thai",
                        subtitle = "Nhiệt độ BBT, que LH, ước tính thụ thai",
                        icon = Icons.Default.Favorite,
                        colorCategory = UtilityColorCategory.ROSE,
                        onClick = { onNavigateToRoute(Screen.ConceptionModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Theo dõi thai kỳ",
                        subtitle = "Tuần thai, đếm cử động thai máy & cơn gò",
                        icon = Icons.Default.ChildCare,
                        colorCategory = UtilityColorCategory.LAVENDER,
                        onClick = { onNavigateToRoute(Screen.PregnancyModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Phục hồi sau sinh",
                        subtitle = "Sản dịch Lochia, cữ bú, tâm lý hậu sản",
                        icon = Icons.Default.Spa,
                        colorCategory = UtilityColorCategory.TERRACOTTA,
                        onClick = { onNavigateToRoute(Screen.PostpartumModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Tiền mãn kinh",
                        subtitle = "Đếm cơn bốc hỏa live, giấc ngủ, thể chất",
                        icon = Icons.Default.WbSunny,
                        colorCategory = UtilityColorCategory.SUN,
                        onClick = { onNavigateToRoute(Screen.PerimenopauseModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Vỉ thuốc tránh thai 28 viên",
                        subtitle = "Tương tác vỉ thuốc, xử trí khi quên uống",
                        icon = Icons.Default.Medication,
                        colorCategory = UtilityColorCategory.WATER,
                        onClick = { onNavigateToRoute(Screen.PillPackModule.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Chăm sóc hàng ngày & Thư giãn
            Text(
                text = "Chăm sóc & Thư giãn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Bài tập sàn chậu Kegel",
                        subtitle = "Đếm nhịp co thắt 5s - thả lỏng 5s chuẩn y khoa",
                        icon = Icons.Default.SelfImprovement,
                        colorCategory = UtilityColorCategory.TERRACOTTA,
                        onClick = { onNavigateToRoute(Screen.KegelModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Tự khám vú định kỳ",
                        subtitle = "4 bước tự kiểm tra mô tuyến vú tại nhà",
                        icon = Icons.Default.HealthAndSafety,
                        colorCategory = UtilityColorCategory.ROSE,
                        onClick = { onNavigateToRoute(Screen.BreastSelfExamModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Trắc nghiệm trầm cảm EPDS",
                        subtitle = "Sàng lọc tâm lý sau sinh 10 câu hỏi chuẩn",
                        icon = Icons.Default.Quiz,
                        colorCategory = UtilityColorCategory.LAVENDER,
                        onClick = { onNavigateToRoute(Screen.EpdsScreeningModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Sức khỏe mỗi ngày",
                        subtitle = "Theo dõi nước uống, giấc ngủ, cân nặng",
                        icon = Icons.Default.FitnessCenter,
                        colorCategory = UtilityColorCategory.WATER,
                        onClick = { onNavigateToRoute(Screen.DailyHealthModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Thư giãn & Bài tập thở",
                        subtitle = "Visual Box Breathing 4-4-4-4 & Thở 4-7-8",
                        icon = Icons.Default.Air,
                        colorCategory = UtilityColorCategory.SAGE,
                        onClick = { onNavigateToRoute(Screen.RelaxationModule.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Sổ khám bệnh & Y bạ",
                        subtitle = "Lịch sử khám, kết quả xét nghiệm, đơn thuốc",
                        icon = Icons.Default.FolderShared,
                        colorCategory = UtilityColorCategory.LAVENDER,
                        onClick = { onNavigateToRoute(Screen.MedicalRecordsModule.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Hệ thống & Bảo mật
            Text(
                text = "Hệ thống & Bảo mật",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Thư viện kiến thức",
                        subtitle = "Cẩm nang y khoa & hướng dẫn ngoại tuyến",
                        icon = Icons.Default.MenuBook,
                        colorCategory = UtilityColorCategory.ROSE,
                        onClick = { onNavigateToRoute(Screen.Knowledge.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Trung tâm nhắc nhở",
                        subtitle = "Báo thức uống nước, nhật ký, thuốc, chu kỳ",
                        icon = Icons.Default.NotificationsActive,
                        colorCategory = UtilityColorCategory.WATER,
                        onClick = { onNavigateToRoute(Screen.ReminderCenter.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Cài đặt & Mã PIN",
                        subtitle = "Khóa ứng dụng, đổi giao diện, ngụy trang",
                        icon = Icons.Default.Settings,
                        colorCategory = UtilityColorCategory.NEUTRAL,
                        onClick = { onNavigateToRoute(Screen.Settings.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Sao lưu & Khôi phục",
                        subtitle = "Xuất/nhập file sao lưu mã hóa ngoại tuyến",
                        icon = Icons.Default.Backup,
                        colorCategory = UtilityColorCategory.SAGE,
                        onClick = { onNavigateToRoute(Screen.BackupRestore.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Tiện ích trực tuyến (Tùy chọn kết nối)
            Text(
                text = "Tiện ích trực tuyến",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column {
                    UtilityItemRow(
                        title = "Tra cứu dinh dưỡng thực phẩm",
                        subtitle = "Tra cứu calo, dưỡng chất qua Open Food Facts",
                        icon = Icons.Default.Restaurant,
                        colorCategory = UtilityColorCategory.WATER,
                        onClick = { onNavigateToRoute(Screen.FoodSearch.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Đề tài sức khỏe phụ nữ",
                        subtitle = "Cẩm nang y tế từ MyHealthfinder v4",
                        icon = Icons.Default.HealthAndSafety,
                        colorCategory = UtilityColorCategory.ROSE,
                        onClick = { onNavigateToRoute(Screen.HealthTopics.route) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Chất lượng không khí (OpenAQ)",
                        subtitle = "Theo dõi trạm quan trắc không khí gần bạn",
                        icon = Icons.Default.Cloud,
                        colorCategory = UtilityColorCategory.SAGE,
                        onClick = { onNavigateToRoute(Screen.AirQuality.createRoute(21.0278, 105.8342)) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), modifier = Modifier.padding(start = 74.dp, end = 16.dp))
                    UtilityItemRow(
                        title = "Quản lý tiện ích trực tuyến",
                        subtitle = "Bật/tắt nguồn dữ liệu, quản lý hạn mức & cache",
                        icon = Icons.Default.Public,
                        colorCategory = UtilityColorCategory.SUN,
                        onClick = { onNavigateToRoute(Screen.OnlineUtilities.route) }
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
    colorCategory: UtilityColorCategory,
    onClick: () -> Unit
) {
    val (containerColor, iconColor) = getUtilityCategoryColors(colorCategory)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(20.dp)
        )
    }
}
