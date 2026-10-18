package com.annhien.womenshealth.feature.online.environment

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.annhien.womenshealth.core.online.environment.WaqiData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQualityScreen(
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit,
    viewModel: AirQualityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedToken by viewModel.aqicnToken.collectAsState()
    var searchCity by remember { mutableStateOf("") }
    var showTokenConfig by remember { mutableStateOf(false) }
    var inputToken by remember(savedToken) {
        mutableStateOf(savedToken.ifBlank { "a91f3c8524d08973aa73870b750414f39de41442" })
    }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadAirQuality(latitude, longitude)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.loadAirQualityUsingDeviceLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chất lượng không khí (AQICN)", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = { showTokenConfig = !showTokenConfig }) {
                        Icon(Icons.Default.VpnKey, contentDescription = "Cấu hình API Token")
                    }
                    IconButton(onClick = { viewModel.loadAirQuality(latitude, longitude) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thanh tìm kiếm trạm quan trắc / Thành phố
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchCity,
                        onValueChange = { searchCity = it },
                        placeholder = { Text("Tìm theo tên thành phố (vd: Hanoi, Saigon...)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                    Button(
                        onClick = {
                            if (searchCity.isNotBlank()) {
                                viewModel.loadAirQualityByCity(searchCity)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tìm")
                    }
                }
            }

            // Nút định vị GPS
            item {
                OutlinedButton(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val coarseGranted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (fineGranted || coarseGranted) {
                            viewModel.loadAirQualityUsingDeviceLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Đo theo trạm gần vị trí GPS hiện tại")
                }
            }

            // Phần cấu hình token AQICN nếu người dùng muốn chỉnh sửa
            item {
                AnimatedVisibility(visible = showTokenConfig) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Cấu hình Token AQICN (aqicn.org)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Token đã được đăng ký và cung cấp. Bạn có thể đổi token nếu cần:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            OutlinedTextField(
                                value = inputToken,
                                onValueChange = { inputToken = it },
                                label = { Text("AQICN API Token") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.saveToken(inputToken, latitude, longitude)
                                        showTokenConfig = false
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Lưu Token")
                                }
                                TextButton(onClick = { showTokenConfig = false }) {
                                    Text("Đóng")
                                }
                            }
                        }
                    }
                }
            }

            // Hiển thị trạng thái dữ liệu
            when (val state = uiState) {
                AirQualityUiState.Idle -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("Nhấn 'Tìm' hoặc 'Đo theo trạm gần vị trí GPS' để xem dữ liệu.", textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                AirQualityUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text("Đang tải dữ liệu chất lượng không khí AQICN...")
                            }
                        }
                    }
                }

                is AirQualityUiState.Success -> {
                    val data = state.data
                    val aqi = data.getAqiValue() ?: 0
                    val aqiInfo = getAqiLevelInfo(aqi)

                    item {
                        // Thẻ Card chính hiển thị AQI
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = aqiInfo.color.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = data.city?.name ?: "Trạm quan trắc địa phương",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                data.time?.s?.let {
                                    Text(
                                        text = "Cập nhật lúc: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(Modifier.height(16.dp))

                                // Vòng tròn hiển thị điểm AQI
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(aqiInfo.color),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = if (aqi > 0) "$aqi" else "--",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "AQI",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(14.dp))

                                Text(
                                    text = aqiInfo.levelName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = aqiInfo.color
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = aqiInfo.advice,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                if (state.fromCache) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "⚡ Đang xem từ bộ nhớ tạm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    // Khuyến nghị sức khỏe riêng biệt cho phụ nữ và thai kỳ
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Lời khuyên sức khỏe cho phụ nữ & thai kỳ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                }
                                Text(
                                    text = getWomenHealthAdvice(aqi),
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 19.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Bảng chi tiết các chất gây ô nhiễm và vi khí hậu
                    item {
                        Text("Chỉ số chi tiết trạm đo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    item {
                        data.iaqi?.let { iaqi ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ParamCard(title = "Bụi mịn PM2.5", value = iaqi.pm25?.v?.let { "$it µg/m³" } ?: "--", modifier = Modifier.weight(1f))
                                    ParamCard(title = "Bụi PM10", value = iaqi.pm10?.v?.let { "$it µg/m³" } ?: "--", modifier = Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ParamCard(title = "Khí Ozone (O₃)", value = iaqi.o3?.v?.let { "$it" } ?: "--", modifier = Modifier.weight(1f))
                                    ParamCard(title = "Khí NO₂", value = iaqi.no2?.v?.let { "$it" } ?: "--", modifier = Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ParamCard(title = "Nhiệt độ", value = iaqi.t?.v?.let { "$it °C" } ?: "--", modifier = Modifier.weight(1f))
                                    ParamCard(title = "Độ ẩm", value = iaqi.h?.v?.let { "$it %" } ?: "--", modifier = Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ParamCard(title = "Áp suất", value = iaqi.p?.v?.let { "$it hPa" } ?: "--", modifier = Modifier.weight(1f))
                                    ParamCard(title = "Gió", value = iaqi.w?.v?.let { "$it m/s" } ?: "--", modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                is AirQualityUiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Lỗi dữ liệu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                }
                                Text(state.message, style = MaterialTheme.typography.bodyMedium)

                                Spacer(Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.loadAirQuality(latitude, longitude) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Thử lại")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

private data class AqiLevelInfo(
    val levelName: String,
    val color: Color,
    val advice: String
)

private fun getAqiLevelInfo(aqi: Int): AqiLevelInfo {
    return when {
        aqi <= 50 -> AqiLevelInfo(
            levelName = "Tốt (0 - 50)",
            color = Color(0xFF2E7D32),
            advice = "Chất lượng không khí trong lành, rất lý tưởng cho các hoạt động thể dục ngoài trời."
        )
        aqi <= 100 -> AqiLevelInfo(
            levelName = "Trung bình (51 - 100)",
            color = Color(0xFFF9A825),
            advice = "Chất lượng không khí chấp nhận được. Những người cực kỳ nhạy cảm có thể cần lưu ý."
        )
        aqi <= 150 -> AqiLevelInfo(
            levelName = "Kém (101 - 150)",
            color = Color(0xFFE65100),
            advice = "Nhóm nhạy cảm, người có bệnh hô hấp hoặc phụ nữ mang thai nên hạn chế ở lâu ngoài trời."
        )
        aqi <= 200 -> AqiLevelInfo(
            levelName = "Xấu (151 - 200)",
            color = Color(0xFFC62828),
            advice = "Bắt đầu ảnh hưởng tới mọi người. Khuyến nghị đeo khẩu trang chống bụi N95 khi ra ngoài."
        )
        aqi <= 300 -> AqiLevelInfo(
            levelName = "Rất xấu (201 - 300)",
            color = Color(0xFF6A1B9A),
            advice = "Cảnh báo sức khỏe khẩn cấp. Mọi người nên ở trong nhà, đóng cửa sổ và bật máy lọc không khí."
        )
        else -> AqiLevelInfo(
            levelName = "Nguy hại (> 300)",
            color = Color(0xFF4E342E),
            advice = "Cực kỳ nguy hiểm cho mọi đối tượng. Tuyệt đối không ra ngoài nếu không thực sự cần thiết."
        )
    }
}

private fun getWomenHealthAdvice(aqi: Int): String {
    return when {
        aqi <= 50 -> "Thời tiết tuyệt vời! Thích hợp để đi dạo nhẹ nhàng, tập yoga ngoài trời, hít thở sâu và thư giãn tinh thần."
        aqi <= 100 -> "Thời tiết ổn định. Mẹ bầu và các bạn gái có cơ địa dị ứng nên uống đủ nước và rửa mũi bằng nước muối sinh lý sau khi về nhà."
        aqi <= 150 -> "Mẹ bầu hoặc phụ nữ có tiền sử hen suyễn, viêm xoang nên mang khẩu trang chuyên dụng (KF94/N95), hạn chế tập thể dục nặng ngoài trời."
        aqi <= 200 -> "Nồng độ bụi mịn PM2.5 cao có thể ảnh hưởng đến làn da và hệ tuần hoàn mẹ bầu. Cần đóng cửa sổ phòng ngủ, bổ sung thực phẩm giàu chất chống oxy hóa (vitamin C, E)."
        else -> "Bụi mịn mức nguy hại. Hãy ở trong phòng kín có điều hòa hoặc lọc khí. Phụ nữ mang thai đặc biệt tránh tiếp xúc khói bụi để bảo vệ sức khỏe mẹ và thai nhi."
    }
}
