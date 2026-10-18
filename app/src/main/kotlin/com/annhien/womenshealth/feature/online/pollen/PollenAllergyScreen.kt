package com.annhien.womenshealth.feature.online.pollen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─── Data Models ─────────────────────────────────────────────────────────────

private data class PollenCityData(
    val id: String,
    val name: String,
    val grass: Int,
    val birch: Int,
    val ragweed: Int,
    val mold: Int,
    val overall: String,
    val riskColor: Color,
    val desc: String
)

private val POLLEN_CITIES = mapOf(
    "hanoi" to PollenCityData(
        id = "hanoi",
        name = "Hà Nội",
        grass = 18, birch = 6, ragweed = 12, mold = 54,
        overall = "Trung bình",
        riskColor = Color(0xFFF9A825),
        desc = "Độ ẩm không khí cao tạo điều kiện cho bào tử nấm mốc phát triển. Người có cơ địa viêm mũi dị ứng nên thận trọng."
    ),
    "hcm" to PollenCityData(
        id = "hcm",
        name = "TP. Hồ Chí Minh",
        grass = 10, birch = 2, ragweed = 5, mold = 32,
        overall = "Thấp",
        riskColor = Color(0xFF43A047),
        desc = "Nồng độ phấn hoa ở ngưỡng an toàn. Phù hợp cho các hoạt động thể dục ngoài trời của phụ nữ mang thai."
    ),
    "dalat" to PollenCityData(
        id = "dalat",
        name = "Đà Lạt",
        grass = 72, birch = 48, ragweed = 35, mold = 24,
        overall = "Cao",
        riskColor = Color(0xFFEF6C00),
        desc = "Mùa hoa và thông nở rộ khiến mật độ phấn hoa trong không khí tăng cao. Khuyến cáo đeo khẩu trang chống dị ứng."
    ),
    "danang" to PollenCityData(
        id = "danang",
        name = "Đà Nẵng",
        grass = 8, birch = 1, ragweed = 4, mold = 18,
        overall = "Rất thấp",
        riskColor = Color(0xFF43A047),
        desc = "Gió biển trong lành cuốn trôi các dị nguyên đường hô hấp. Không khí rất tốt cho hệ hô hấp mẹ & bé."
    )
)

// ─── Main Composable ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollenAllergyScreen(onNavigateBack: () -> Unit) {
    var selectedCityKey by remember { mutableStateOf("hanoi") }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val city = POLLEN_CITIES[selectedCityKey] ?: POLLEN_CITIES.values.first()
    val primaryColor = Color(0xFF3D684E)
    val sageBg = Color(0xFFD8E7DB)

    fun refreshData() {
        isRefreshing = true
        coroutineScope.launch {
            delay(700)
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dự báo Phấn hoa & Dị ứng", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(Modifier.height(8.dp))

            // Intro Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = sageBg.copy(alpha = 0.45f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Text("🌸", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Open-Meteo Air Quality Pollen API",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Dự báo nồng độ phấn hoa cỏ, cây gỗ, cúc dại & bào tử nấm mốc theo thời gian thực. Không cần API Key. Cache TTL 6 giờ.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // City Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                POLLEN_CITIES.values.forEach { c ->
                    val active = selectedCityKey == c.id
                    Button(
                        onClick = {
                            selectedCityKey = c.id
                            refreshData()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) primaryColor else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "📍 ${c.name}",
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Đang kết nối Open-Meteo Air Quality Pollen API...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Overall Risk Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp),
                    border = BorderStroke(1.5.dp, city.riskColor.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(city.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(
                                    "Cảnh báo dị ứng môi trường",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = city.riskColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "Mức ${city.overall}",
                                    color = city.riskColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            city.desc,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Pollen 4 Indicators Grid
                Text("Chỉ số 4 tác nhân gây dị ứng chính", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PollenIndicatorCard(
                        iconTitle = "🌾 Phấn hoa cỏ",
                        value = city.grass,
                        unit = "grains / m³",
                        subtitle = "Nguyên nhân chính gây hắt hơi sổ mũi dị ứng thời tiết",
                        isHigh = city.grass > 50,
                        primaryColor = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    PollenIndicatorCard(
                        iconTitle = "🌳 Phấn hoa gỗ",
                        value = city.birch,
                        unit = "grains / m³",
                        subtitle = "Phấn hoa cây bạch dương & thông gây ngứa mí mắt",
                        isHigh = city.birch > 30,
                        primaryColor = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PollenIndicatorCard(
                        iconTitle = "🌼 Cúc dại Ragweed",
                        value = city.ragweed,
                        unit = "grains / m³",
                        subtitle = "Kích ứng niêm mạc mũi họng và kích phát hen",
                        isHigh = city.ragweed > 30,
                        primaryColor = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                    PollenIndicatorCard(
                        iconTitle = "🍄 Bào tử nấm mốc",
                        value = city.mold,
                        unit = "spores / m³",
                        subtitle = "Tăng cao khi thời tiết nồm ẩm hoặc mưa kéo dài",
                        isHigh = city.mold > 40,
                        primaryColor = primaryColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Health Recommendations
                Text("Khuyến cáo bảo vệ sức khỏe phụ nữ & thai phụ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text("🫁", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Vệ sinh đường thở an toàn trong thai kỳ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Phụ nữ mang thai cơ địa viêm xoang nên rửa mũi bằng nước muối sinh lý ấm 0.9% hoặc xịt nước biển sâu 2 lần/ngày. Tránh tự ý uống thuốc kháng dị ứng kháng histamin nhóm C mà chưa có chỉ định của bác sĩ sản phụ khoa.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text("🏠", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Khung giờ vàng tránh phấn hoa", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Mật độ hạt phấn phát tán cao nhất từ 5h00 đến 9h00 sáng. Nên đóng kín cửa sổ phòng ngủ trong khung giờ này, phơi đồ trong nhà có mái che và thay áo quần, gội đầu sau khi đi ngoài đường về.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { refreshData() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cập nhật dự báo phấn hoa Open-Meteo")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ─── Sub-composables ─────────────────────────────────────────────────────────

@Composable
private fun PollenIndicatorCard(
    iconTitle: String,
    value: Int,
    unit: String,
    subtitle: String,
    isHigh: Boolean,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.dp, if (isHigh) Color(0xFFEF6C00).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(iconTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    "$value",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isHigh) Color(0xFFEF6C00) else primaryColor
                )
            }
            Text(unit, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Text(
                subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}
