package com.annhien.womenshealth.feature.online.workouts

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Data models ──────────────────────────────────────────────────────────────

private data class WorkoutCategory(
    val id: String, val name: String, val emoji: String,
    val tag: String, val color: Color, val bgColor: Color,
    val exercises: List<WorkoutExercise>
)

private data class WorkoutExercise(
    val name: String, val sets: String, val reps: String,
    val duration: String = "", val note: String = ""
)

private val WORKOUT_CATEGORIES = listOf(
    WorkoutCategory(
        "kegel", "Sàn chậu Kegel", "🌸", "Nền tảng", Color(0xFF854E62), Color(0xFFFFD9E2),
        listOf(
            WorkoutExercise("Kegel cơ bản", "3 hiệp", "10 lần", "5s co - 5s thả", "Co cơ đáy chậu như khi nín tiểu"),
            WorkoutExercise("Kegel nhanh (Flick)", "3 hiệp", "20 lần", "1s co - 1s thả", "Tăng tốc độ co cơ phản xạ"),
            WorkoutExercise("Kegel kéo dài", "3 hiệp", "5 lần", "10s co - 10s thả", "Tăng sức bền cơ sàn chậu")
        )
    ),
    WorkoutCategory(
        "pregnancy", "Vận động thai kỳ", "🤰", "An toàn", Color(0xFF5F5B7D), Color(0xFFE5E0F9),
        listOf(
            WorkoutExercise("Đi bộ nhẹ nhàng", "1 lần", "20–30 phút", "", "Tốc độ vừa phải, tránh trơn trượt"),
            WorkoutExercise("Yoga thai kỳ – Tư thế mèo bò", "3 hiệp", "8 lần", "", "Giảm đau lưng, giãn cột sống"),
            WorkoutExercise("Kegel thai kỳ", "5 hiệp", "10 lần", "5s co", "Phòng rò rỉ tiểu sau sinh"),
            WorkoutExercise("Nghiêng xương chậu", "3 hiệp", "10 lần", "", "Nằm nghiêng trái, nâng hông nhẹ")
        )
    ),
    WorkoutCategory(
        "postpartum", "Phục hồi sau sinh", "💛", "6 tuần+", Color(0xFFA83800), Color(0xFFFFE0D2),
        listOf(
            WorkoutExercise("Kegel hậu sản", "4 hiệp", "10 lần", "5s", "Bắt đầu ngay ngày đầu sau sinh"),
            WorkoutExercise("Thở hoành cách mô", "5 phút", "Liên tục", "", "Kích hoạt lại cơ bụng sâu"),
            WorkoutExercise("Nâng đầu gối 90°", "3 hiệp", "8 mỗi bên", "", "Chỉ khi không còn tách cơ bụng"),
            WorkoutExercise("Squat cơ bản", "3 hiệp", "10 lần", "", "Từ tuần thứ 6 sau khi được bác sĩ cho phép")
        )
    ),
    WorkoutCategory(
        "stretching", "Giãn cơ & Thư giãn", "🧘", "Hàng ngày", Color(0xFF3D684E), Color(0xFFD8E7DB),
        listOf(
            WorkoutExercise("Giãn cơ vai cổ", "3 lần", "30 giây/tư thế", "", "Hữu ích khi ngồi làm việc lâu"),
            WorkoutExercise("Pigeon Pose (Bồ câu)", "2 lần", "45 giây/bên", "", "Mở hông, giảm đau vùng chậu"),
            WorkoutExercise("Child Pose (Trẻ em)", "3 lần", "60 giây", "", "Thư giãn toàn thân, thở đều"),
            WorkoutExercise("Twisting Spine (Xoắn cột sống)", "2 lần", "30 giây/bên", "", "Cải thiện tiêu hóa")
        )
    ),
    WorkoutCategory(
        "strength", "Tăng sức mạnh", "💪", "Trung cấp", Color(0xFF5F5B7D), Color(0xFFE5E0F9),
        listOf(
            WorkoutExercise("Squat sumo", "3 hiệp", "12 lần", "", "Mở hông, tăng sức mạnh đùi"),
            WorkoutExercise("Hip Thrust", "3 hiệp", "15 lần", "", "Mông, đùi sau, cơ sàn chậu"),
            WorkoutExercise("Plank", "3 hiệp", "30 giây", "", "Kiểm tra tách cơ bụng trước"),
            WorkoutExercise("Lunge luân phiên", "3 hiệp", "10 lần/chân", "", "Cân bằng, đùi, mông")
        )
    )
)

// ─── Kegel timer state ────────────────────────────────────────────────────────

private enum class KegelPhase { READY, CONTRACT, RELAX }

// ─── Main Composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(onNavigateBack: () -> Unit) {
    var selectedCatId by remember { mutableStateOf("kegel") }
    var expandedExercise by remember { mutableStateOf<String?>(null) }

    // Kegel timer
    var kegelRunning by remember { mutableStateOf(false) }
    var kegelPhase by remember { mutableStateOf(KegelPhase.READY) }
    var kegelSec by remember { mutableStateOf(5) }
    var kegelReps by remember { mutableStateOf(0) }

    val scrollState = rememberScrollState()
    val selectedCat = WORKOUT_CATEGORIES.first { it.id == selectedCatId }

    // Kegel timer effect
    LaunchedEffect(kegelRunning) {
        if (!kegelRunning) return@LaunchedEffect
        kegelPhase = KegelPhase.CONTRACT
        kegelSec = 5
        while (kegelRunning) {
            delay(1000)
            kegelSec--
            if (kegelSec <= 0) {
                if (kegelPhase == KegelPhase.CONTRACT) {
                    kegelPhase = KegelPhase.RELAX
                    kegelSec = 5
                } else {
                    kegelPhase = KegelPhase.CONTRACT
                    kegelSec = 5
                    kegelReps++
                }
            }
        }
    }

    val ringColor = when (kegelPhase) {
        KegelPhase.CONTRACT -> Color(0xFF854E62)
        KegelPhase.RELAX -> Color(0xFF3D684E)
        KegelPhase.READY -> MaterialTheme.colorScheme.primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bài tập thể dục phụ nữ", fontWeight = FontWeight.Bold) },
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

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Text("🏋️", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Wger Workout Manager API", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Bài tập được thiết kế đặc biệt cho phụ nữ ở mỗi giai đoạn sức khỏe — thai kỳ, hậu sản, tiền mãn kinh.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Interactive Kegel timer (visible when kegel category selected)
            if (selectedCatId == "kegel") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F8))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⏱ Bộ đếm Kegel tương tác", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(14.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(if (kegelPhase == KegelPhase.CONTRACT) Color(0xFFFFD9E2) else Color(0xFFD8E7DB))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (kegelPhase == KegelPhase.READY) "🌸" else kegelSec.toString(),
                                    fontSize = if (kegelPhase == KegelPhase.READY) 36.sp else 40.sp,
                                    fontWeight = FontWeight.ExtraBold, color = ringColor
                                )
                                Text(
                                    when (kegelPhase) {
                                        KegelPhase.READY -> "SẴN SÀNG"
                                        KegelPhase.CONTRACT -> "CO CƠ"
                                        KegelPhase.RELAX -> "THẢ LỎNG"
                                    },
                                    fontSize = 10.sp, color = ringColor, fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (kegelPhase == KegelPhase.READY) "Nhấn Bắt đầu để đồng bộ nhịp tập"
                            else if (kegelPhase == KegelPhase.CONTRACT) "Co cơ đáy chậu như khi nín tiểu"
                            else "Thả lỏng hoàn toàn, hít thở đều",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (kegelRunning) {
                                    kegelRunning = false
                                    kegelPhase = KegelPhase.READY
                                    kegelSec = 5
                                } else {
                                    kegelReps = 0
                                    kegelRunning = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (kegelRunning) MaterialTheme.colorScheme.error else ringColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                if (kegelRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null, modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (kegelRunning) "Dừng lại" else "Bắt đầu Kegel")
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Đã hoàn thành: $kegelReps hiệp",
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
            }

            // Category selector
            Text("📚 Chọn nhóm bài tập", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))

            WORKOUT_CATEGORIES.forEach { cat ->
                val isSelected = selectedCatId == cat.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedCatId = cat.id },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) cat.bgColor else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) cat.color else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.emoji, fontSize = 20.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(cat.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = if (isSelected) cat.color else MaterialTheme.colorScheme.onSurface)
                            Text("${cat.exercises.size} bài tập • ${cat.tag}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (isSelected) Icon(Icons.Default.CheckCircle, null, tint = cat.color, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Exercise list
            Text("💪 Danh sách bài tập — ${selectedCat.name}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))

            selectedCat.exercises.forEachIndexed { i, ex ->
                val isExpanded = expandedExercise == "${selectedCatId}_$i"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { expandedExercise = if (isExpanded) null else "${selectedCatId}_$i" },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (isExpanded) selectedCat.color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(32.dp).clip(CircleShape).background(selectedCat.bgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = selectedCat.color)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(ex.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = selectedCat.bgColor) {
                                        Text(ex.sets, fontSize = 10.sp, color = selectedCat.color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = selectedCat.bgColor) {
                                        Text(ex.reps, fontSize = 10.sp, color = selectedCat.color, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                    if (ex.duration.isNotEmpty()) {
                                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                            Text(ex.duration, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                            Icon(
                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isExpanded && ex.note.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "💡 ${ex.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "🌐 Dữ liệu bài tập: Wger Workout Manager API (cache 30 ngày)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
