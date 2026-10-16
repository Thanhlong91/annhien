package com.annhien.womenshealth.feature.knowledge

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BuiltInArticle(
    val id: String,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val isApproved: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeScreen(
    onNavigateBack: () -> Unit
) {
    val articles = remember {
        listOf(
            BuiltInArticle(
                id = "art_1",
                title = "Hiểu đúng về chu kỳ kinh nguyệt và các pha sinh học",
                category = "Chu kỳ",
                summary = "Chu kỳ kinh nguyệt của phụ nữ dao động tự nhiên từ 21 đến 35 ngày và gồm 4 pha chính.",
                content = "Chu kỳ kinh nguyệt không chỉ đơn thuần là những ngày hành kinh. Nó phản ánh sự biến đổi nhịp nhàng của các hormone Estrogen và Progesterone qua 4 giai đoạn:\n\n" +
                        "1. Pha hành kinh (Menstrual phase): Niêm mạc tử cung bong ra khi thụ thai không diễn ra.\n" +
                        "2. Pha nang noãn (Follicular phase): Nang trứng phát triển dưới tác động của FSH, cơ thể bắt đầu tăng sinh năng lượng.\n" +
                        "3. Pha rụng trứng (Ovulatory phase): Trứng trưởng thành được phóng thích từ buồng trứng.\n" +
                        "4. Pha hoàng thể (Luteal phase): Hoàng thể sản sinh progesterone để nuôi dưỡng niêm mạc. Nếu không có thai, nồng độ hormone giảm và chu kỳ mới bắt đầu.\n\n" +
                        "Việc theo dõi chu kỳ giúp bạn nhận diện nhịp điệu tự nhiên của cơ thể và chuẩn bị tốt nhất cho sức khỏe thể chất."
            ),
            BuiltInArticle(
                id = "art_2",
                title = "Lưu ý an toàn: Tại sao không dùng app dự đoán để tránh thai tự nhiên?",
                category = "An toàn",
                summary = "Các thuật toán lịch chỉ dự đoán trung vị và không thể đảm bảo an toàn tuyệt đối trước sự thay đổi hormone bất ngờ.",
                content = "Rất nhiều người lầm tưởng rằng chỉ cần tránh quan hệ vào 'những ngày đỏ' được app tính toán là có thể tránh thai an toàn. Đây là một ngộ nhận tiềm ẩn nhiều rủi ro.\n\n" +
                        "Lý do khoa học:\n" +
                        "• Thời điểm rụng trứng chịu tác động lớn bởi stress, thiếu ngủ, thay đổi cân nặng hoặc bệnh lý tạm thời.\n" +
                        "• Tinh trùng có thể tồn tại tới 5 ngày trong đường sinh sản của phụ nữ.\n" +
                        "• Trứng sau khi rụng có thể thụ tinh trong vòng 12-24 giờ.\n\n" +
                        "Vì vậy, An Nhiên cam kết không bao giờ hiển thị nhãn 'ngày an toàn' và khuyến nghị bạn luôn sử dụng các biện pháp tránh thai chủ động đã được y khoa chứng minh."
            ),
            BuiltInArticle(
                id = "art_3",
                title = "Dấu hiệu cảnh báo phụ khoa cần đến gặp bác sĩ ngay",
                category = "Y tế",
                summary = "Những triệu chứng bất thường không nên tự theo dõi tại nhà mà cần thăm khám y tế chuyên khoa.",
                content = "Hãy đặt lịch hẹn với bác sĩ chuyên khoa nếu bạn gặp bất kỳ dấu hiệu nào sau đây:\n\n" +
                        "• Đau bụng dưới dữ dội, không thuyên giảm sau khi dùng thuốc giảm đau thông thường.\n" +
                        "• Xuất huyết âm đạo bất thường giữa chu kỳ hoặc sau khi quan hệ.\n" +
                        "• Sản dịch sau sinh có mùi hôi nồng nặc hoặc sốt cao trên 38°C.\n" +
                        "• Rong kinh kéo dài trên 7 ngày với lượng máu rất nhiều (thấm đẫm băng vệ sinh mỗi giờ).\n" +
                        "• Khí hư có màu xanh, vàng đục kèm ngứa rát kéo dài.\n\n" +
                        "Chăm sóc sức khỏe phụ nữ bắt đầu từ sự lắng nghe và hành động kịp thời."
            ),
            BuiltInArticle(
                id = "art_4",
                title = "Hơi thở và nhịp tim: Kiểm soát căng thẳng bằng Box Breathing",
                category = "Thư giãn",
                summary = "Phương pháp thở 4-4-4-4 giúp kích hoạt hệ thần kinh phó giao cảm và xoa dịu lo âu.",
                content = "Hơi thở là cầu nối duy nhất giữa hệ thần kinh tự chủ và ý chí chủ động của con người.\n\n" +
                        "Khi căng thẳng, nhịp thở trở nên nông và nhanh, kích hoạt phản ứng 'chiến đấu hay bỏ chạy'. Khi bạn chủ động kéo dài nhịp thở và giữ hơi thở theo tỉ lệ cân bằng 4 giây hít - 4 giây giữ - 4 giây thở - 4 giây tĩnh, dây thần kinh phế vị (vagus nerve) gửi tín hiệu an toàn đến tim và não bộ, giúp hạ nhịp tim và giảm áp lực động mạch.\n\n" +
                        "Hãy thực hành 5 phút mỗi ngày tại mục 'Thư giãn & Hơi thở' trong An Nhiên để duy trì năng lượng tích cực."
            )
        )
    }

    var selectedArticle by remember { mutableStateOf<BuiltInArticle?>(null) }
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    val categories = listOf("Tất cả", "Chu kỳ", "An toàn", "Y tế", "Thư giãn")

    val filteredArticles = remember(selectedCategory) {
        if (selectedCategory == "Tất cả") articles
        else articles.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedArticle != null) "Nội dung bài viết" else "Thư viện kiến thức", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedArticle != null) selectedArticle = null
                            else onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (selectedArticle != null) {
            // Article Detail View
            val article = selectedArticle!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = article.category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = article.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 26.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else {
            // Article List View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Category Pills
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredArticles) { art ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { selectedArticle = art },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = art.category,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = art.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = art.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
