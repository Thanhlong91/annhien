package com.annhien.womenshealth.feature.postpartum

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.domain.model.EpdsRiskCategory

data class EpdsQuestion(
    val id: Int,
    val questionVi: String,
    val optionsVi: List<Pair<String, Int>> // Text -> Điểm
)

val EPDS_QUESTIONS_LIST = listOf(
    EpdsQuestion(
        1,
        "1. Tôi có thể cười và nhìn thấy mặt hài hước của mọi việc:",
        listOf(
            "Như trước đây tôi vẫn vậy" to 0,
            "Bây giờ không được nhiều như trước" to 1,
            "Bây giờ rõ ràng ít hơn trước nhiều" to 2,
            "Hoàn toàn không thể" to 3
        )
    ),
    EpdsQuestion(
        2,
        "2. Tôi cảm thấy hào hứng hướng về tương lai:",
        listOf(
            "Nhiều như trước đây tôi vẫn vậy" to 0,
            "Hơi ít hơn trước đây một chút" to 1,
            "Rõ ràng ít hơn trước đây" to 2,
            "Hầu như không bao giờ hào hứng" to 3
        )
    ),
    EpdsQuestion(
        3,
        "3. Tôi tự trách mình vô cớ khi mọi việc diễn ra không như ý:",
        listOf(
            "Vâng, hầu như luôn luôn như vậy" to 3,
            "Vâng, thỉnh thoảng tôi tự trách mình" to 2,
            "Hiếm khi tôi làm vậy" to 1,
            "Không bao giờ" to 0
        )
    ),
    EpdsQuestion(
        4,
        "4. Tôi cảm thấy lo lắng hoặc bồn chồn vô cớ:",
        listOf(
            "Không bao giờ" to 0,
            "Hiếm khi" to 1,
            "Có, thỉnh thoảng" to 2,
            "Có, rất thường xuyên" to 3
        )
    ),
    EpdsQuestion(
        5,
        "5. Tôi cảm thấy hoảng sợ hoặc sợ hãi mà không rõ lý do cụ thể:",
        listOf(
            "Có, khá nhiều lần" to 3,
            "Có, đôi khi" to 2,
            "Không, không nhiều" to 1,
            "Hoàn toàn không" to 0
        )
    ),
    EpdsQuestion(
        6,
        "6. Mọi công việc chăm sóc và gia đình dường như quá tải đối với tôi:",
        listOf(
            "Có, hầu như tôi không thể đối phó nổi" to 3,
            "Có, đôi khi tôi không đối phó tốt như thường lệ" to 2,
            "Không, phần lớn thời gian tôi vẫn xoay xở được" to 1,
            "Không, tôi vẫn quán xuyến mọi việc bình thường" to 0
        )
    ),
    EpdsQuestion(
        7,
        "7. Tôi cảm thấy buồn bã đến mức khó ngủ ngon giấc:",
        listOf(
            "Có, hầu như suốt đêm" to 3,
            "Có, đôi khi" to 2,
            "Hiếm khi" to 1,
            "Hoàn toàn không" to 0
        )
    ),
    EpdsQuestion(
        8,
        "8. Tôi cảm thấy buồn bã hoặc khổ sở trong lòng:",
        listOf(
            "Có, hầu như suốt ngày" to 3,
            "Có, khá thường xuyên" to 2,
            "Hiếm khi" to 1,
            "Hoàn toàn không" to 0
        )
    ),
    EpdsQuestion(
        9,
        "9. Tôi cảm thấy buồn bã đến mức chỉ muốn khóc:",
        listOf(
            "Có, hầu như mọi lúc" to 3,
            "Có, rất thường xuyên" to 2,
            "Chỉ thỉnh thoảng thôi" to 1,
            "Không bao giờ" to 0
        )
    ),
    EpdsQuestion(
        10,
        "10. Ý nghĩ tự làm hại bản thân từng xuất hiện trong tâm trí tôi:",
        listOf(
            "Có, khá thường xuyên" to 3,
            "Thỉnh thoảng" to 2,
            "Rất hiếm khi" to 1,
            "Không bao giờ" to 0
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpdsScreeningScreen(
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Map câu hỏi (1..10) -> điểm (0..3)
    val answers = remember { mutableStateMapOf<Int, Int>().apply {
        EPDS_QUESTIONS_LIST.forEach { put(it.id, 0) }
    } }

    var showResult by remember { mutableStateOf(false) }

    val totalScore = answers.values.sum()
    val q10Score = answers[10] ?: 0

    val category = when {
        totalScore >= 13 -> EpdsRiskCategory.HIGH_RISK
        totalScore >= 10 -> EpdsRiskCategory.MODERATE_RISK
        else -> EpdsRiskCategory.LOW_RISK
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sàng lọc trầm cảm EPDS", fontWeight = FontWeight.Bold) },
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

            // Giới thiệu
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thang đo Edinburgh Postnatal Depression Scale",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "10 câu hỏi trắc nghiệm tâm lý chuẩn y khoa quốc tế. Hãy chọn câu trả lời mô tả gần đúng nhất cảm xúc của bạn trong 7 ngày qua.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Danh sách 10 câu hỏi
            EPDS_QUESTIONS_LIST.forEachIndexed { index, question ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = question.questionVi,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val selectedScore = answers[question.id] ?: 0

                        question.optionsVi.forEach { (text, score) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        answers[question.id] = score
                                        showResult = false
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedScore == score,
                                    onClick = {
                                        answers[question.id] = score
                                        showResult = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selectedScore == score) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedScore == score) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showResult = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Xem kết quả đánh giá tâm lý")
            }

            // Kết quả đánh giá
            if (showResult) {
                Spacer(modifier = Modifier.height(20.dp))

                val resultCardColor = when (category) {
                    EpdsRiskCategory.LOW_RISK -> Color(0xFF2E7D32).copy(alpha = 0.1f)
                    EpdsRiskCategory.MODERATE_RISK -> Color(0xFFF57C00).copy(alpha = 0.15f)
                    EpdsRiskCategory.HIGH_RISK -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                }

                val resultTextColor = when (category) {
                    EpdsRiskCategory.LOW_RISK -> Color(0xFF2E7D32)
                    EpdsRiskCategory.MODERATE_RISK -> Color(0xFFE65100)
                    EpdsRiskCategory.HIGH_RISK -> MaterialTheme.colorScheme.error
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = resultCardColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Điểm tổng: $totalScore / 30",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = resultTextColor
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = resultTextColor
                            ) {
                                Text(
                                    text = category.labelVi.substringBefore(" ("),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = category.clinicalAdviceVi,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )

                        if (q10Score > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bạn có ghi nhận suy nghĩ tiêu cực ở câu 10. Hãy chia sẻ ngay với người thân hoặc liên hệ đường dây nóng tâm lý để được lắng nghe và hỗ trợ kịp thời.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
