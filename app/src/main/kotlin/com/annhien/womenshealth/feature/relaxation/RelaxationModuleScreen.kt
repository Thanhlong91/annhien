package com.annhien.womenshealth.feature.relaxation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class BreathingTechnique(
    val titleVi: String,
    val subtitleVi: String,
    val tagVi: String,
    val inhaleSec: Int,
    val holdSec: Int,
    val exhaleSec: Int,
    val holdAfterSec: Int,
    val instructionVi: String
) {
    BOX(
        titleVi = "Thở hộp (Box Breathing)",
        subtitleVi = "4-4-4-4 • Giảm stress & cân bằng thần kinh",
        tagVi = "Giảm căng thẳng",
        inhaleSec = 4,
        holdSec = 4,
        exhaleSec = 4,
        holdAfterSec = 4,
        instructionVi = "Hít vào 4s, giữ hơi 4s, thở ra 4s và tĩnh lặng 4s. Thích hợp khi gặp áp lực công việc hoặc lo âu đột ngột."
    ),
    RELAX_478(
        titleVi = "Thở ru ngủ sâu (4-7-8)",
        subtitleVi = "4-7-8 • Chống mất ngủ & ngủ ngon",
        tagVi = "Ngủ ngon",
        inhaleSec = 4,
        holdSec = 7,
        exhaleSec = 8,
        holdAfterSec = 0,
        instructionVi = "Hít sâu qua mũi 4s, nín thở 7s, thở ra bằng miệng 8s. Kích hoạt hệ phó giao cảm giúp nhịp tim chậm lại và chìm vào giấc ngủ."
    ),
    DIAPHRAGMATIC(
        titleVi = "Thở bụng cơ hoành (4-6)",
        subtitleVi = "4-6 • Giảm đau bụng kinh & thư giãn chậu",
        tagVi = "Đau bụng kinh",
        inhaleSec = 4,
        holdSec = 0,
        exhaleSec = 6,
        holdAfterSec = 0,
        instructionVi = "Đặt 1 tay lên ngực, 1 tay lên bụng. Hít vào phình bụng 4s, thở ra hóp bụng 6s. Giúp giãn cơ trơn tử cung và giải phóng áp lực vùng chậu."
    ),
    COHERENT(
        titleVi = "Thở cân bằng nhịp tim (5-5)",
        subtitleVi = "5-5 • Đồng bộ nhịp tim sinh học HRV",
        tagVi = "Tập trung & Năng lượng",
        inhaleSec = 5,
        holdSec = 0,
        exhaleSec = 5,
        holdAfterSec = 0,
        instructionVi = "Hít vào 5s, thở ra 5s đều đặn (6 nhịp/phút). Đồng bộ nhịp tim và hệ thần kinh, tạo sự tỉnh táo và tập trung cho ngày mới."
    ),
    COOLING(
        titleVi = "Thở làm mát cơn bốc hỏa (4-2-6)",
        subtitleVi = "4-2-6 • Hạ nhiệt & tiền mãn kinh",
        tagVi = "Tiền mãn kinh",
        inhaleSec = 4,
        holdSec = 2,
        exhaleSec = 6,
        holdAfterSec = 0,
        instructionVi = "Hít vào từ tốn qua môi khép hờ để đón luồng khí mát, giữ 2s, thở chậm qua mũi 6s. Xua tan cảm giác nóng bừng và toát mồ hôi."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxationModuleScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Điều hòa hơi thở, 1: Wim Hof
    var isSessionActive by remember { mutableStateOf(false) }
    var selectedTechnique by remember { mutableStateOf(BreathingTechnique.BOX) }
    var breathingPhase by remember { mutableStateOf("Sẵn sàng") }
    var secondsInPhase by remember { mutableStateOf(0) }
    var completedCycles by remember { mutableStateOf(0) }

    // Visual breathing circle animation scale
    val targetScale = when (breathingPhase) {
        "Hít vào thật sâu" -> 1.45f
        "Giữ hơi thở" -> 1.45f
        "Thở ra từ từ" -> 0.88f
        "Tĩnh lặng" -> 0.88f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = when (breathingPhase) {
                "Hít vào thật sâu" -> selectedTechnique.inhaleSec * 1000
                "Thở ra từ từ" -> selectedTechnique.exhaleSec * 1000
                "Giữ hơi thở" -> 400
                "Tĩnh lặng" -> 400
                else -> 500
            },
            easing = FastOutSlowInEasing
        ),
        label = "BreathingScale"
    )

    val activeColor by animateColorAsState(
        targetValue = when (breathingPhase) {
            "Hít vào thật sâu" -> MaterialTheme.colorScheme.primary
            "Giữ hơi thở" -> MaterialTheme.colorScheme.tertiary
            "Thở ra từ từ" -> MaterialTheme.colorScheme.secondary
            "Tĩnh lặng" -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.primary
        },
        label = "PhaseColor"
    )

    // Breathing loop
    LaunchedEffect(isSessionActive, selectedTechnique) {
        if (isSessionActive) {
            while (isSessionActive) {
                // Inhale
                breathingPhase = "Hít vào thật sâu"
                for (s in selectedTechnique.inhaleSec downTo 1) {
                    secondsInPhase = s
                    delay(1000)
                }

                // Hold
                if (selectedTechnique.holdSec > 0) {
                    breathingPhase = "Giữ hơi thở"
                    for (s in selectedTechnique.holdSec downTo 1) {
                        secondsInPhase = s
                        delay(1000)
                    }
                }

                // Exhale
                breathingPhase = "Thở ra từ từ"
                for (s in selectedTechnique.exhaleSec downTo 1) {
                    secondsInPhase = s
                    delay(1000)
                }

                // Hold after exhale (Box breathing)
                if (selectedTechnique.holdAfterSec > 0) {
                    breathingPhase = "Tĩnh lặng"
                    for (s in selectedTechnique.holdAfterSec downTo 1) {
                        secondsInPhase = s
                        delay(1000)
                    }
                }

                completedCycles += 1
            }
        } else {
            breathingPhase = "Sẵn sàng"
            secondsInPhase = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thư giãn & Điều hòa hơi thở", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        isSessionActive = false
                        selectedTab = 0
                    },
                    text = { Text("Hơi thở điều hòa") },
                    icon = { Icon(Icons.Default.Air, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        isSessionActive = false
                        selectedTab = 1
                    },
                    text = { Text("Phương pháp Wim Hof") },
                    icon = { Icon(Icons.Default.AcUnit, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Technique Selection Carousel
                Text(
                    text = "Chọn bài tập thở:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BreathingTechnique.values()) { technique ->
                    val isSelected = selectedTechnique == technique
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable {
                                isSessionActive = false
                                selectedTechnique = technique
                                completedCycles = 0
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Badge(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(technique.tagVi, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = technique.titleVi,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = technique.subtitleVi,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Animated Breathing Visualizer
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(activeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isSessionActive) "$secondsInPhase" else "✦",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = breathingPhase,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSessionActive) "Chu kỳ nhịp thở: $completedCycles vòng hoàn thành"
                else selectedTechnique.instructionVi,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { isSessionActive = !isSessionActive },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSessionActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isSessionActive) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSessionActive) "Dừng bài tập" else "Bắt đầu ${selectedTechnique.titleVi}",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Relaxation Guide 1: PMR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thư giãn cơ tiến triển (PMR)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Giải phóng căng thẳng tích tụ trong cơ bắp qua 4 bước tuần tự:\n" +
                                "1. Ngón chân & Bắp chân: Gập siết chặt 5s rồi thở ra buông lỏng hoàn toàn.\n" +
                                "2. Vùng đùi & Đáy chậu: Gồng nhẹ cơ mông và sàn chậu 5s rồi nhả mềm.\n" +
                                "3. Bụng & Ngực: Hít sâu giữ căng lồng ngực rồi thở mạnh giải tỏa.\n" +
                                "4. Vai & Cơ mặt: Nhấc vai chạm mang tai, nhăn trán rồi thở hắt thả rơi tự nhiên.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Relaxation Guide 2: 5-4-3-2-1 Grounding
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Neo giữ thực tại xoa dịu lo âu (5-4-3-2-1)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Khi cảm thấy choáng ngợp hay hoảng loạn, hãy nhìn quanh và gọi tên:\n" +
                                "• 5 đồ vật bạn nhìn thấy\n" +
                                "• 4 bề mặt bạn có thể chạm vào\n" +
                                "• 3 âm thanh bạn nghe thấy lúc này\n" +
                                "• 2 mùi hương bạn ngửi được\n" +
                                "• 1 vị bạn cảm nhận trong miệng\n\n" +
                                "Giúp não bộ kích hoạt phản xạ an toàn và đưa nhịp tim trở lại bình thường.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        } else {
            // TAB 1: PHƯƠNG PHÁP THỞ WIM HOF
            WimHofBreathingCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Kiến thức y khoa về Wim Hof
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cơ chế sinh học của phương pháp Wim Hof",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Tăng thông khí có kiểm soát (30 nhịp): Thải nhanh CO2 ra khỏi máu, nâng nhẹ độ pH (kiềm hóa máu tạm thời), kích thích phản xạ tỉnh táo.\n\n" +
                                "2. Nín thở yếm khí (Retention): Khi nồng độ CO2 thấp, bạn có thể nín thở rất lâu mà không có cảm giác ngộp thở. Cơ thể kích hoạt phản xạ sinh tồn tế bào và giải phóng các chất chống viêm tự nhiên.\n\n" +
                                "3. Nhịp phục hồi 15 giây: Nạp đầy oxy trở lại cho các mô não và tim, tạo cảm giác sảng khoái, hưng phấn và tái tạo năng lượng.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
        }
    }
}
