package com.annhien.womenshealth.feature.online.quotes

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ─── Quote data ───────────────────────────────────────────────────────────────

private data class InspirationalQuote(
    val textVi: String, val textEn: String, val author: String,
    val category: String, val emoji: String
)

private val QUOTES = listOf(
    InspirationalQuote(
        "Cơ thể bạn không phải là kẻ thù — hãy lắng nghe nó như một người bạn thân.",
        "Your body is not your enemy — listen to it like a dear friend.",
        "An Nhiên Wellness", "Sức khỏe", "🌿"
    ),
    InspirationalQuote(
        "Tự chăm sóc bản thân không phải ích kỷ. Đó là điều kiện để bạn có thể chăm sóc người khác.",
        "Self-care is not selfish. It is the prerequisite for caring for others.",
        "Audre Lorde", "Bình an", "💆"
    ),
    InspirationalQuote(
        "Hãy cho phép cơ thể bạn nghỉ ngơi mà không cảm thấy có lỗi.",
        "Give yourself permission to rest without guilt.",
        "Brené Brown", "Sức khỏe", "🌸"
    ),
    InspirationalQuote(
        "Sức mạnh của phụ nữ không đến từ việc cứng rắn mà đến từ sự dịu dàng không bao giờ vơi cạn.",
        "The strength of a woman comes not from hardness but from endless tenderness.",
        "Maya Angelou", "Sức mạnh", "💪"
    ),
    InspirationalQuote(
        "Mỗi ngày bạn còn thở đều là một ngày để bắt đầu lại.",
        "Each day you breathe is a chance to begin again.",
        "Thich Nhat Hanh", "Hy vọng", "🌅"
    ),
    InspirationalQuote(
        "Tiến độ, không phải sự hoàn hảo.",
        "Progress, not perfection.",
        "Brené Brown", "Động lực", "✨"
    ),
    InspirationalQuote(
        "Một cơ thể khỏe mạnh là nơi trú ngụ tốt nhất cho tâm hồn mạnh mẽ.",
        "A healthy body is the best home for a strong soul.",
        "Francis Bacon", "Sức khỏe", "🏡"
    ),
    InspirationalQuote(
        "Giấc ngủ là đội ngũ y tế miễn phí mà cơ thể bạn tự trang bị mỗi đêm.",
        "Sleep is the free medical team your body deploys every night.",
        "Matthew Walker", "Giấc ngủ", "🌙"
    ),
    InspirationalQuote(
        "Yêu thương bản thân là nền tảng của mọi sự yêu thương khác.",
        "Self-love is the foundation of all other loves.",
        "Oscar Wilde", "Bình an", "❤️"
    ),
    InspirationalQuote(
        "Chu kỳ của bạn là nhịp điệu tự nhiên — không phải gánh nặng cần che giấu.",
        "Your cycle is a natural rhythm — not a burden to hide.",
        "An Nhiên Wellness", "Phụ nữ", "🌺"
    )
)

// ─── Main Composable ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuotesScreen(onNavigateBack: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var showVi by remember { mutableStateOf(true) }
    var copied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val quote = QUOTES[currentIndex]

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lời khích lệ mỗi ngày", fontWeight = FontWeight.Bold) },
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
                    Text("💬", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Quotable.io API + Bộ sưu tập An Nhiên", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Trích dẫn truyền cảm hứng về sức khỏe, thể chất và bình an nội tâm. Cập nhật mỗi ngày.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main quote card with gradient
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF5F5B7D).copy(alpha = 0.15f), Color(0xFFD8E7DB).copy(alpha = 0.2f))
                            )
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(quote.emoji, fontSize = 40.sp)
                        Spacer(Modifier.height(14.dp))

                        AnimatedContent(
                            targetState = "$currentIndex-$showVi",
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                            },
                            label = "quote_text"
                        ) {
                            Text(
                                text = if (showVi) "\"${quote.textVi}\"" else "\"${quote.textEn}\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 26.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Text(
                            "— ${quote.author}",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                            Text(
                                "✨ ${quote.category}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Shuffle
                OutlinedButton(
                    onClick = { currentIndex = (0 until QUOTES.size).random() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Shuffle, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Câu khác", fontSize = 13.sp)
                }

                // Language toggle
                OutlinedButton(
                    onClick = { showVi = !showVi },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (showVi) "🇬🇧 EN" else "🇻🇳 VI", fontSize = 13.sp)
                }

                // Copy
                Button(
                    onClick = {
                        val text = if (showVi) "\"${quote.textVi}\" — ${quote.author}"
                        else "\"${quote.textEn}\" — ${quote.author}"
                        clipboardManager.setText(AnnotatedString(text))
                        copied = true
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copied) Color(0xFF3D684E) else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        null, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (copied) "Đã sao" else "Sao chép", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quote counter chips
            Text("📚 Tất cả lời khích lệ (${QUOTES.size} câu)", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(10.dp))

            QUOTES.forEachIndexed { i, q ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable { currentIndex = i },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (i == currentIndex)
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        if (i == currentIndex) 2.dp else 1.dp,
                        if (i == currentIndex) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(32.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) { Text(q.emoji, fontSize = 16.sp) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                q.textVi.take(60) + if (q.textVi.length > 60) "..." else "",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontStyle = FontStyle.Italic
                            )
                            Text("— ${q.author} • ${q.category}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (i == currentIndex) {
                            Icon(Icons.Default.VolumeUp, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "🌐 Nguồn: Quotable.io API + An Nhiên Wellness Collection  •  📥 Cache ngoại tuyến 7 ngày",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
