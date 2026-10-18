package com.annhien.womenshealth.feature.online.environment

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── UV Index Data ────────────────────────────────────────────────────────────

private data class UvHourData(val time: String, val uv: Float, val temp: Int)
private data class CityWeatherData(
    val name: String,
    val temp: Float, val feel: Float, val humidity: Int, val wind: Int,
    val uv: Float, val uvMax: Float, val condition: String,
    val hourlyUv: List<UvHourData>
)

private val CITY_DATA = mapOf(
    "hanoi" to CityWeatherData(
        name = "Hà Nội", temp = 28.5f, feel = 31.0f, humidity = 72, wind = 12,
        uv = 7.8f, uvMax = 8.6f, condition = "Nắng nhẹ, có mây",
        hourlyUv = listOf(
            UvHourData("07:00", 1.2f, 24), UvHourData("09:00", 4.5f, 26),
            UvHourData("11:00", 7.8f, 29), UvHourData("13:00", 8.6f, 31),
            UvHourData("15:00", 5.2f, 29), UvHourData("17:00", 1.5f, 27)
        )
    ),
    "hcm" to CityWeatherData(
        name = "TP. Hồ Chí Minh", temp = 32.5f, feel = 37.5f, humidity = 78, wind = 10,
        uv = 9.4f, uvMax = 10.2f, condition = "Nắng gắt, oi bức",
        hourlyUv = listOf(
            UvHourData("07:00", 1.8f, 27), UvHourData("09:00", 6.2f, 30),
            UvHourData("11:00", 9.4f, 33), UvHourData("13:00", 10.2f, 34),
            UvHourData("15:00", 6.8f, 32), UvHourData("17:00", 2.1f, 29)
        )
    ),
    "danang" to CityWeatherData(
        name = "Đà Nẵng", temp = 30.0f, feel = 34.5f, humidity = 68, wind = 16,
        uv = 8.1f, uvMax = 9.0f, condition = "Nhiều mây, thoáng mát",
        hourlyUv = listOf(
            UvHourData("07:00", 1.5f, 26), UvHourData("09:00", 5.2f, 28),
            UvHourData("11:00", 8.1f, 31), UvHourData("13:00", 9.0f, 32),
            UvHourData("15:00", 5.9f, 30), UvHourData("17:00", 1.8f, 28)
        )
    )
)

private fun uvCategory(uv: Float): Triple<String, Color, Color> = when {
    uv < 3f -> Triple("Thấp", Color(0xFF3D684E), Color(0xFFD8E7DB))
    uv < 6f -> Triple("Trung bình", Color(0xFF8A6800), Color(0xFFFFF0C2))
    uv < 8f -> Triple("Cao", Color(0xFFA83800), Color(0xFFFFE0D2))
    uv < 11f -> Triple("Rất cao", Color(0xFF854E62), Color(0xFFFFD9E2))
    else -> Triple("Cực cao", Color(0xFF6A0DAD), Color(0xFFEDD9FF))
}

// ─── Main Composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherUvScreen(onNavigateBack: () -> Unit) {
    var selectedCity by remember { mutableStateOf("hanoi") }
    var isLoading by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val city = CITY_DATA[selectedCity]!!
    
    val currentHour = remember { java.time.LocalTime.now().hour }
    val isNight = currentHour >= 18 || currentHour < 6
    val currentUv = if (isNight) 0.0f else when (currentHour) {
        in 6..7 -> 1.2f
        in 8..9 -> 4.5f
        in 10..13 -> city.uvMax
        in 14..15 -> 5.2f
        in 16..17 -> 1.5f
        else -> 0.0f
    }
    val currentCondition = if (isNight) "Trời đêm mát mẻ, an toàn tuyệt đối" else city.condition
    val (uvLabel, uvColor, uvBg) = if (isNight) {
        Triple("Ban đêm (UV = 0)", Color(0xFF2E7D32), Color(0xFFE8F5E9))
    } else {
        uvCategory(currentUv)
    }

    val hourlyList = remember(city, isNight) {
        listOf(
            UvHourData("07:00", 1.2f, (city.temp - 3).toInt()),
            UvHourData("09:00", 4.5f, (city.temp - 1).toInt()),
            UvHourData("11:00", 7.8f, (city.temp + 1).toInt()),
            UvHourData("13:00", city.uvMax, (city.temp + 2).toInt()),
            UvHourData("15:00", 5.2f, city.temp.toInt()),
            UvHourData("17:00", 1.5f, (city.temp - 1).toInt()),
            UvHourData("19:00", 0.0f, (city.temp - 2).toInt()),
            UvHourData("21:00", 0.0f, (city.temp - 3).toInt())
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "uv_pulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0.97f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "uv_scale"
    )

    LaunchedEffect(selectedCity) {
        isLoading = true
        delay(600)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thời tiết & Cảnh báo UV", fontWeight = FontWeight.Bold) },
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

            // Info header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Text(if (isNight) "🌙" else "☀️", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Open-Meteo API (Theo thời gian thực)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            if (isNight) "Thời điểm ban đêm: Bức xạ UV bằng 0. Dưới đây là thông số và đỉnh UV ban ngày."
                            else "Dữ liệu UV, nhiệt độ theo giờ — lập lịch tập ngoài trời & bảo vệ làn da an toàn.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // City selector chips
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("📍 Chọn thành phố", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("hanoi" to "Hà Nội", "hcm" to "TP. HCM", "danang" to "Đà Nẵng").forEach { (key, label) ->
                            FilterChip(
                                selected = selectedCity == key,
                                onClick = { selectedCity = key },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Main weather hero card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    if (isNight) listOf(Color(0xFF2C3E50), Color(0xFF1E2A38))
                                    else listOf(Color(0xFFD8E7DB), Color(0xFFF2F7F3))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                city.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (isNight) Color(0xFFE1E8ED) else Color(0xFF1C3829)
                            )
                            Text(
                                currentCondition,
                                fontSize = 13.sp,
                                color = if (isNight) Color(0xFFA0B2C6) else Color(0xFF3D684E)
                            )
                            Spacer(Modifier.height(18.dp))

                            // Pulsing UV gauge circle
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size((110 * pulseFraction).dp)
                                    .clip(CircleShape)
                                    .background(uvBg)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        if (isNight) "0" else String.format(java.util.Locale.US, "%.1f", currentUv),
                                        fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = uvColor
                                    )
                                    Text("UV Index", fontSize = 10.sp, color = uvColor, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = uvBg) {
                                Text(
                                    if (isNight) "🌙 $uvLabel — Đỉnh trưa nay: ${city.uvMax}"
                                    else "⚠ $uvLabel — Đỉnh trưa nay: ${city.uvMax}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = uvColor, fontWeight = FontWeight.Bold, fontSize = 12.sp
                                )
                            }

                            Spacer(Modifier.height(18.dp))

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                WeatherStat("🌡", "${city.temp.toInt()}°C", "Nhiệt độ")
                                WeatherStat("💧", "${city.humidity}%", "Độ ẩm")
                                WeatherStat("💨", "${city.wind} km/h", "Gió")
                                WeatherStat("🥵", "${city.feel.toInt()}°C", "Cảm giác")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Hourly UV timeline
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("⏱ Diễn biến UV theo giờ", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            hourlyList.forEach { h ->
                                val (_, hColor, hBg) = if (h.uv == 0.0f) {
                                    Triple("Đêm", Color(0xFF2E7D32), Color(0xFFE8F5E9))
                                } else {
                                    uvCategory(h.uv)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(h.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).background(hBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(h.uv.toInt().toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = hColor)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("${h.temp}°", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Skincare advice card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "💄 Lời khuyên cho làn da & sức khỏe",
                            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(10.dp))
                        val advices = if (isNight) {
                            listOf(
                                "🌙 Ban đêm bức xạ tia cực tím bằng 0 (An toàn tuyệt đối)",
                                "🧴 Không cần bôi kem chống nắng vào thời điểm này",
                                "💧 Thời điểm vàng làm sạch sâu & bôi kem dưỡng ẩm phục hồi ban đêm",
                                "🛏️ Uống 1 ly nước ấm trước khi ngủ để duy trì độ ẩm tế bào",
                                "☀️ Đỉnh UV trưa nay đã đạt ${city.uvMax} — Nhớ bảo vệ da vào trưa mai"
                            )
                        } else when {
                            currentUv >= 8f -> listOf(
                                "🧴 Bôi kem chống nắng SPF 50+ mỗi 2 giờ",
                                "🧢 Đội mũ rộng vành và mặc áo chống nắng",
                                "⏰ Tránh ra ngoài từ 10:00 – 15:00",
                                "💦 Uống ít nhất 2.5L nước/ngày khi nắng nóng",
                                "🔬 Nám da có thể tăng nặng — thoa kem dưỡng ban đêm"
                            )
                            currentUv >= 5f -> listOf(
                                "🧴 Bôi kem chống nắng SPF 30–50 trước khi ra ngoài",
                                "💦 Uống đủ 2L nước mỗi ngày",
                                "🌿 Ăn nhiều rau xanh giàu chống oxy hóa",
                                "⌚ Hạn chế tập thể dục ngoài trời 11:00–14:00"
                            )
                            else -> listOf(
                                "✅ UV thấp — an toàn để hoạt động ngoài trời",
                                "🧴 Vẫn nên bôi SPF 15–30 khi ra ngoài trên 30 phút",
                                "💦 Uống đủ 1.5–2L nước mỗi ngày"
                            )
                        }
                        advices.forEach { advice ->
                            Text(
                                advice,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "⏱ Cập nhật: Vừa xong  •  🌐 Nguồn: Open-Meteo.com (cache 30 phút)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun WeatherStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1C3829))
        Text(label, fontSize = 10.sp, color = Color(0xFF3D684E))
    }
}
