package com.annhien.womenshealth.feature.postpartum

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class KegelState {
    IDLE,
    SQUEEZE, // Co thắt
    RELAX,   // Thả lỏng
    FINISHED // Hoàn thành hiệp
}

@Composable
fun KegelTrainerCard(
    modifier: Modifier = Modifier,
    onSessionCompleted: (repetitions: Int) -> Unit = {}
) {
    var isRunning by remember { mutableStateOf(false) }
    var currentState by remember { mutableStateOf(KegelState.IDLE) }
    var currentRep by remember { mutableStateOf(1) }
    val totalReps = 10
    var secondsRemaining by remember { mutableStateOf(3) }
    val squeezeDuration = 3 // giây co
    val relaxDuration = 3   // giây thả lỏng
    var completedSessionsToday by remember { mutableStateOf(0) }

    // Timer loop
    LaunchedEffect(isRunning, currentState) {
        if (!isRunning) return@LaunchedEffect

        if (currentState == KegelState.IDLE) {
            currentState = KegelState.SQUEEZE
            secondsRemaining = squeezeDuration
        }

        while (isActive && isRunning) {
            delay(1000L)
            if (secondsRemaining > 1) {
                secondsRemaining -= 1
            } else {
                // Đổi trạng thái
                if (currentState == KegelState.SQUEEZE) {
                    currentState = KegelState.RELAX
                    secondsRemaining = relaxDuration
                } else if (currentState == KegelState.RELAX) {
                    if (currentRep < totalReps) {
                        currentRep += 1
                        currentState = KegelState.SQUEEZE
                        secondsRemaining = squeezeDuration
                    } else {
                        // Kết thúc buổi tập
                        currentState = KegelState.FINISHED
                        isRunning = false
                        completedSessionsToday += 1
                        onSessionCompleted(totalReps)
                        break
                    }
                }
            }
        }
    }

    // Animation progress cho vòng tròn
    val animatedProgress by animateFloatAsState(
        targetValue = when (currentState) {
            KegelState.IDLE -> 0f
            KegelState.SQUEEZE -> 1f - (secondsRemaining.toFloat() / squeezeDuration)
            KegelState.RELAX -> 1f - (secondsRemaining.toFloat() / relaxDuration)
            KegelState.FINISHED -> 1f
        },
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "KegelProgress"
    )

    // Pulse animation cho vòng tròn khi co thắt
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary
    val activeColor by animateColorAsState(
        targetValue = when (currentState) {
            KegelState.SQUEEZE -> primaryColor
            KegelState.RELAX -> secondaryColor
            KegelState.FINISHED -> Color(0xFF2E7D32) // Xanh lá hoàn thành
            KegelState.IDLE -> MaterialTheme.colorScheme.outline
        },
        label = "StateColor"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Luyện tập cơ sàn chậu (Kegel)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hỗ trợ phục hồi tử cung & kiểm soát tiểu tiện",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vòng tròn luyện tập tương tác
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    // Vòng nền mờ
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Vòng tiến trình
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentState) {
                        KegelState.IDLE -> {
                            Text(
                                text = "Sẵn sàng",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "10 nhịp / buổi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        KegelState.SQUEEZE -> {
                            Text(
                                text = "SIẾT CHẶT",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor
                            )
                            Text(
                                text = "${secondsRemaining}s",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Text(
                                text = "Nhịp $currentRep / $totalReps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        KegelState.RELAX -> {
                            Text(
                                text = "THẢ LỎNG",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = secondaryColor
                            )
                            Text(
                                text = "${secondsRemaining}s",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = secondaryColor
                            )
                            Text(
                                text = "Nhịp $currentRep / $totalReps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        KegelState.FINISHED -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Xuất sắc!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Đã xong 10 nhịp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hướng dẫn kỹ thuật
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentState == KegelState.SQUEEZE) {
                            "Siết cơ như đang nín tiểu. Không nín thở, hít thở đều qua mũi."
                        } else if (currentState == KegelState.RELAX) {
                            "Thả lỏng hoàn toàn cơ đáy chậu để máu lưu thông phục hồi."
                        } else {
                            "Mẹo: Ngồi thoải mái trên ghế hoặc nằm ngửa, co nhẹ hai đầu gối."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nút bấm điều khiển
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isRunning && currentState != KegelState.FINISHED) {
                    Button(
                        onClick = {
                            isRunning = true
                            if (currentState == KegelState.IDLE) {
                                currentRep = 1
                                currentState = KegelState.SQUEEZE
                                secondsRemaining = squeezeDuration
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (currentState == KegelState.IDLE) "Bắt đầu tập" else "Tiếp tục")
                    }
                } else if (isRunning) {
                    Button(
                        onClick = { isRunning = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tạm dừng")
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            currentState = KegelState.IDLE
                            currentRep = 1
                            secondsRemaining = squeezeDuration
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Đặt lại")
                    }
                } else {
                    // Trạng thái FINISHED
                    Button(
                        onClick = {
                            currentState = KegelState.IDLE
                            currentRep = 1
                            secondsRemaining = squeezeDuration
                            isRunning = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tập lại hiệp mới")
                    }
                }
            }

            if (completedSessionsToday > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hôm nay bạn đã hoàn thành $completedSessionsToday bài tập sàn chậu.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
