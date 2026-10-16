package com.annhien.womenshealth.feature.relaxation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class WimHofPhase {
    IDLE,
    POWER_BREATHING, // 30 nhịp hít sâu
    RETENTION,       // Nín thở tối đa sau khi thở ra
    RECOVERY,        // Hít sâu giữ 15 giây
    ROUND_FINISHED   // Hoàn thành hiệp
}

@Composable
fun WimHofBreathingCard(
    modifier: Modifier = Modifier
) {
    var phase by remember { mutableStateOf(WimHofPhase.IDLE) }
    var currentBreathRep by remember { mutableStateOf(1) }
    val targetBreathReps = 30
    var isSubInhale by remember { mutableStateOf(true) }

    var retentionSeconds by remember { mutableStateOf(0) }
    var bestRetentionSeconds by remember { mutableStateOf(0) }
    var recoverySecondsRemaining by remember { mutableStateOf(15) }
    var currentRound by remember { mutableStateOf(1) }

    var showSafetyWarningDialog by remember { mutableStateOf(false) }
    var hasAcceptedSafety by remember { mutableStateOf(false) }

    // Logic loop cho Wim Hof
    LaunchedEffect(phase) {
        when (phase) {
            WimHofPhase.POWER_BREATHING -> {
                currentBreathRep = 1
                while (isActive && phase == WimHofPhase.POWER_BREATHING) {
                    // Hít sâu 1.5s
                    isSubInhale = true
                    delay(1500)
                    // Thở buông lỏng 1.5s
                    isSubInhale = false
                    delay(1500)

                    if (currentBreathRep < targetBreathReps) {
                        currentBreathRep++
                    } else {
                        // Chuyển sang giai đoạn nín thở
                        phase = WimHofPhase.RETENTION
                        retentionSeconds = 0
                        break
                    }
                }
            }
            WimHofPhase.RETENTION -> {
                retentionSeconds = 0
                while (isActive && phase == WimHofPhase.RETENTION) {
                    delay(1000)
                    retentionSeconds++
                }
            }
            WimHofPhase.RECOVERY -> {
                recoverySecondsRemaining = 15
                while (isActive && phase == WimHofPhase.RECOVERY && recoverySecondsRemaining > 0) {
                    delay(1000)
                    recoverySecondsRemaining--
                }
                if (phase == WimHofPhase.RECOVERY) {
                    if (retentionSeconds > bestRetentionSeconds) {
                        bestRetentionSeconds = retentionSeconds
                    }
                    phase = WimHofPhase.ROUND_FINISHED
                }
            }
            else -> {}
        }
    }

    // Animation scale cho vòng tròn
    val scaleTarget = when (phase) {
        WimHofPhase.POWER_BREATHING -> if (isSubInhale) 1.45f else 0.9f
        WimHofPhase.RETENTION -> 0.85f
        WimHofPhase.RECOVERY -> 1.5f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = scaleTarget,
        animationSpec = tween(
            durationMillis = if (phase == WimHofPhase.POWER_BREATHING) 1400 else 600,
            easing = FastOutSlowInEasing
        ),
        label = "WimHofScale"
    )

    val activeColor by animateColorAsState(
        targetValue = when (phase) {
            WimHofPhase.POWER_BREATHING -> if (isSubInhale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
            WimHofPhase.RETENTION -> Color(0xFFE65100) // Cam cảnh báo nín thở
            WimHofPhase.RECOVERY -> Color(0xFF2E7D32)  // Xanh lá phục hồi
            WimHofPhase.ROUND_FINISHED -> MaterialTheme.colorScheme.primary
            WimHofPhase.IDLE -> MaterialTheme.colorScheme.outline
        },
        label = "WimHofColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AcUnit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Phương pháp Wim Hof (WHM)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hiệp $currentRound • Tăng đề kháng & miễn dịch",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showSafetyWarningDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Cảnh báo y tế",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Banner cảnh báo quan trọng
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "CHỐNG CHỈ ĐỊNH: Phụ nữ đang mang thai & người có bệnh tim/động kinh. Luôn ngồi hoặc nằm khi tập.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vòng tròn trực quan
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(animatedScale)
                        .clip(CircleShape)
                        .background(activeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        when (phase) {
                            WimHofPhase.IDLE -> {
                                Text(
                                    text = "30 nhịp",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            WimHofPhase.POWER_BREATHING -> {
                                Text(
                                    text = "$currentBreathRep/$targetBreathReps",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isSubInhale) "HÍT SÂU" else "THẢ LỎNG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            WimHofPhase.RETENTION -> {
                                val min = retentionSeconds / 60
                                val sec = retentionSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", min, sec),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "NÍN THỞ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            WimHofPhase.RECOVERY -> {
                                Text(
                                    text = "${recoverySecondsRemaining}s",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "GIỮ HƠI",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            WimHofPhase.ROUND_FINISHED -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Text trạng thái
            Text(
                text = when (phase) {
                    WimHofPhase.IDLE -> "Sẵn sàng hiệp $currentRound"
                    WimHofPhase.POWER_BREATHING -> "Giai đoạn 1: 30 nhịp thở sâu liên tục"
                    WimHofPhase.RETENTION -> "Giai đoạn 2: Nín thở sau khi thở ra nhẹ"
                    WimHofPhase.RECOVERY -> "Giai đoạn 3: Hít đầy lồng ngực & giữ 15s"
                    WimHofPhase.ROUND_FINISHED -> "Hoàn thành hiệp $currentRound!"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (phase) {
                    WimHofPhase.IDLE -> "Hít sâu làm đầy bụng rồi đến ngực, buông lỏng ra tự nhiên. Lặp lại 30 lần."
                    WimHofPhase.POWER_BREATHING -> "Có thể thấy ngứa ran đầu ngón tay hoặc lâng lâng nhẹ (phản ứng kiềm hóa máu an toàn)."
                    WimHofPhase.RETENTION -> "Thả lỏng toàn thân. Khi cơ thể muốn thở lại tự nhiên, hãy nhấn nút 'Hít phục hồi' bên dưới."
                    WimHofPhase.RECOVERY -> "Nạp lại oxy cho tế bào não và cơ bắp. Thư giãn đầu óc."
                    WimHofPhase.ROUND_FINISHED -> "Thời gian nín thở hiệp này: ${retentionSeconds}s. Bạn có thể nghỉ 1 phút rồi tập hiệp kế tiếp."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            when (phase) {
                WimHofPhase.IDLE -> {
                    Button(
                        onClick = {
                            if (!hasAcceptedSafety) {
                                showSafetyWarningDialog = true
                            } else {
                                phase = WimHofPhase.POWER_BREATHING
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bắt đầu hiệp $currentRound (30 nhịp)")
                    }
                }
                WimHofPhase.POWER_BREATHING -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                phase = WimHofPhase.IDLE
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Dừng")
                        }
                        Button(
                            onClick = {
                                // Cho phép bỏ qua sớm nếu muốn vào ngay Retention
                                phase = WimHofPhase.RETENTION
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Nín thở ngay")
                        }
                    }
                }
                WimHofPhase.RETENTION -> {
                    Button(
                        onClick = {
                            phase = WimHofPhase.RECOVERY
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.North, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tôi muốn thở lại (Hít giữ 15s)")
                    }
                }
                WimHofPhase.RECOVERY -> {
                    OutlinedButton(
                        onClick = {
                            phase = WimHofPhase.ROUND_FINISHED
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Bỏ qua 15s phục hồi")
                    }
                }
                WimHofPhase.ROUND_FINISHED -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                phase = WimHofPhase.IDLE
                                currentRound = 1
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Kết thúc")
                        }
                        Button(
                            onClick = {
                                currentRound++
                                phase = WimHofPhase.POWER_BREATHING
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Hiệp tiếp theo ($currentRound)")
                        }
                    }
                }
            }

            if (bestRetentionSeconds > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Kỷ lục nín thở buổi tập: ${bestRetentionSeconds / 60}m ${bestRetentionSeconds % 60}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Safety Warning Dialog
    if (showSafetyWarningDialog) {
        AlertDialog(
            onDismissRequest = { showSafetyWarningDialog = false },
            icon = { Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Lưu ý an toàn y khoa bắt buộc") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Phương pháp Wim Hof tạo ra hiện tượng giảm CO2 và thiếu oxy yếm khí tạm thời rất mạnh. Bạn vui lòng đọc kỹ trước khi tập:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "1. TUYỆT ĐỐI KHÔNG TẬP KHI MANG THAI vì có thể ảnh hưởng lưu lượng máu nuôi thai nhi.\n" +
                                "2. Không tập nếu có tiền sử động kinh hoặc bệnh tim mạch.\n" +
                                "3. CHỈ TẬP KHI NGỒI HOẶC NẰM trên giường/thảm êm. Tuyệt đối không tập dưới nước, khi đang đứng hoặc đang lái xe.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hasAcceptedSafety = true
                        showSafetyWarningDialog = false
                        phase = WimHofPhase.POWER_BREATHING
                    }
                ) {
                    Text("Tôi hiểu & Bắt đầu tập")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafetyWarningDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}
