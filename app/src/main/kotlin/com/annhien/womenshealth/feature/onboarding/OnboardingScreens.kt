package com.annhien.womenshealth.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.domain.model.LifeStage

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Center hero illustration/badge
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Chào mừng bạn đến với An Nhiên",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Không gian chăm sóc sức khỏe phụ nữ độc lập, kín đáo và an toàn 100% ngoại tuyến.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Trust highlights
            FeatureHighlight(
                icon = Icons.Default.WifiOff,
                title = "100% Hoạt động ngoại tuyến",
                subtitle = "Không cần mạng, không có máy chủ thu thập dữ liệu cá nhân."
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureHighlight(
                icon = Icons.Default.Lock,
                title = "Mã hóa cấp quân sự",
                subtitle = "Toàn bộ dữ liệu của bạn được mã hóa trực tiếp trên thiết bị bằng SQLCipher."
            )
            Spacer(modifier = Modifier.height(16.dp))
            FeatureHighlight(
                icon = Icons.Default.HealthAndSafety,
                title = "Tôn trọng y khoa",
                subtitle = "Không đưa ra phán đoán tùy tiện, phân định rõ ràng giữa dữ liệu và tham khảo."
            )
        }

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Bắt đầu thiết lập", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LifeStageSelectionScreen(
    selectedStage: LifeStage,
    onStageSelected: (LifeStage) -> Unit,
    onContinue: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Giai đoạn của bạn hiện tại?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "An Nhiên sẽ điều chỉnh các tính năng phù hợp nhất với giai đoạn sức khỏe của bạn. Bạn có thể thay đổi bất cứ lúc nào.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            StageOptionCard(
                stage = LifeStage.CYCLE,
                title = "Theo dõi chu kỳ kinh nguyệt",
                desc = "Ghi chép ngày hành kinh, dự đoán chu kỳ tiếp theo, quản lý triệu chứng PMS.",
                icon = Icons.Default.DateRange,
                isSelected = selectedStage == LifeStage.CYCLE,
                onClick = { onStageSelected(LifeStage.CYCLE) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StageOptionCard(
                stage = LifeStage.CONCEPTION,
                title = "Kế hoạch thụ thai",
                desc = "Theo dõi nhiệt độ cơ thể nền BBT, que thử rụng trứng và các dấu hiệu hỗ trợ.",
                icon = Icons.Default.Favorite,
                isSelected = selectedStage == LifeStage.CONCEPTION,
                onClick = { onStageSelected(LifeStage.CONCEPTION) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StageOptionCard(
                stage = LifeStage.PREGNANCY,
                title = "Đang mang thai",
                desc = "Đếm tuần thai, ngày dự sinh, đếm cử động thai máy và bộ đếm cơn co thắt.",
                icon = Icons.Default.ChildCare,
                isSelected = selectedStage == LifeStage.PREGNANCY,
                onClick = { onStageSelected(LifeStage.PREGNANCY) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StageOptionCard(
                stage = LifeStage.POSTPARTUM,
                title = "Phục hồi sau sinh",
                desc = "Theo dõi sản dịch, tâm trạng hậu sản, cử bú của bé và bài tập sàn chậu.",
                icon = Icons.Default.AccessibilityNew,
                isSelected = selectedStage == LifeStage.POSTPARTUM,
                onClick = { onStageSelected(LifeStage.POSTPARTUM) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            StageOptionCard(
                stage = LifeStage.PERIMENOPAUSE,
                title = "Tiền mãn kinh & Mãn kinh",
                desc = "Ghi nhận cơn bốc hỏa, rối loạn chu kỳ, giấc ngủ và thay đổi thể chất.",
                icon = Icons.Default.WbSunny,
                isSelected = selectedStage == LifeStage.PERIMENOPAUSE,
                onClick = { onStageSelected(LifeStage.PERIMENOPAUSE) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Tiếp tục", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeatureHighlight(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StageOptionCard(
    stage: LifeStage,
    title: String,
    desc: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}
