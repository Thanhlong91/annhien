package com.annhien.womenshealth.feature.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.core.preferences.AppPreferences
import com.annhien.womenshealth.core.security.PinManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    appPreferences: AppPreferences,
    onNavigateToBackup: () -> Unit,
    onNavigateToOnlineUtilities: () -> Unit,
    onNavigateBack: () -> Unit,
    onLockApp: ((disguise: Boolean) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val isLockEnabled by appPreferences.isAppLockEnabled.collectAsState(initial = false)
    val isBiometricEnabled by appPreferences.isBiometricEnabled.collectAsState(initial = false)
    val isCamouflageEnabled by appPreferences.isCamouflageEnabled.collectAsState(initial = false)
    val isSecureScreen by appPreferences.secureScreenEnabled.collectAsState(initial = true)
    val themeMode by appPreferences.themeMode.collectAsState(initial = "SYSTEM")

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinConfirmInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt & Bảo mật", fontWeight = FontWeight.Bold) },
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

            // ── 1. GIAO DIỆN HIỂN THỊ (SÁNG / TỐI / HỆ THỐNG) ──
            Text(
                text = "Giao diện hiển thị",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chế độ giao diện (Dark / Light)",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tùy chỉnh màu sắc sáng hoặc tối phù hợp với mắt bạn",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionCard(
                            title = "Sáng",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == "LIGHT",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch { appPreferences.setThemeMode("LIGHT") }
                            }
                        )
                        ThemeOptionCard(
                            title = "Tối",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == "DARK",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch { appPreferences.setThemeMode("DARK") }
                            }
                        )
                        ThemeOptionCard(
                            title = "Hệ thống",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = themeMode == "SYSTEM",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                coroutineScope.launch { appPreferences.setThemeMode("SYSTEM") }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 2. BẢO MẬT VÀ MÃ PIN ──
            Text(
                text = "Bảo mật riêng tư & Mã PIN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Khóa ứng dụng bằng mã PIN", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Yêu cầu mã PIN mỗi khi mở hoặc rời ứng dụng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    pinInput = ""
                                    pinConfirmInput = ""
                                    pinError = null
                                    showPinDialog = true
                                } else {
                                    coroutineScope.launch {
                                        appPreferences.clearPin()
                                    }
                                }
                            }
                        )
                    }

                    if (isLockEnabled) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Nút đổi PIN & Khóa ngay
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pinInput = ""
                                    pinConfirmInput = ""
                                    pinError = null
                                    showPinDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Đổi mã PIN", fontSize = 13.sp)
                            }

                            Button(
                                onClick = { onLockApp?.invoke(false) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Khóa ngay", fontSize = 13.sp)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mở khóa bằng sinh trắc học", fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Dùng vân tay hoặc khuôn mặt để mở nhanh",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch { appPreferences.setBiometricEnabled(checked) }
                                }
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Bảo vệ ảnh chụp màn hình (FLAG_SECURE)", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Ngăn chụp màn hình và ẩn nội dung trong trình chuyển ứng dụng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSecureScreen,
                            onCheckedChange = { checked ->
                                coroutineScope.launch { appPreferences.setSecureScreenEnabled(checked) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 3. CHẾ ĐỘ NGỤY TRANG MÁY TÍNH ──
            Text(
                text = "Chế độ ngụy trang (Camouflage Disguise)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF9F0A).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = Color(0xFFFF9F0A),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Chế độ ngụy trang máy tính bỏ túi",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Biến toàn bộ giao diện thành máy tính thông thường. Nhập mã PIN rồi nhấn dấu \"=\" để mở lại An Nhiên.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tự động ngụy trang khi khóa",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isCamouflageEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch {
                                    appPreferences.setCamouflageEnabled(checked)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onLockApp?.invoke(true) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bật chế độ ngụy trang ngay", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4. DỮ LIỆU & SAO LƯU ──
            Text(
                text = "Quản lý dữ liệu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToBackup),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sao lưu & Khôi phục dữ liệu", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Xuất/nhập file sao lưu mã hóa ngoại tuyến an toàn",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToOnlineUtilities),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tiện ích trực tuyến", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Bật/tắt tra cứu thực phẩm, kiến thức sức khỏe và chất lượng không khí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── 5. CAM KẾT NGOẠI TUYẾN ──
            Text(
                text = "Thông tin & Cam kết ngoại tuyến",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "An Nhiên — Phiên bản 1.0.0",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Lõi sức khỏe hoạt động ngoại tuyến\n" +
                                "• Mã hóa cơ sở dữ liệu trên thiết bị (SQLCipher)\n" +
                                "• Không theo dõi, không quảng cáo, không máy chủ từ xa\n" +
                                "• Tiện ích trực tuyến là tùy chọn và có thể tắt riêng từng nguồn.\n" +
                                "• Dữ liệu thuộc về bạn duy nhất trên thiết bị này.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Hộp thoại thiết lập / đổi mã PIN
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = {
                    Text(
                        text = if (isLockEnabled) "Đổi mã PIN mới" else "Thiết lập mã PIN khóa app",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "Nhập mã PIN 4 chữ số để khóa bảo vệ dữ liệu sức khỏe của bạn:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("Mã PIN (4 số)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pinConfirmInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                    pinConfirmInput = it
                                    pinError = null
                                }
                            },
                            label = { Text("Xác nhận lại mã PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (pinError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pinError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pinInput.length != 4) {
                                pinError = "Vui lòng nhập đủ 4 chữ số"
                            } else if (pinInput != pinConfirmInput) {
                                pinError = "Mã PIN xác nhận không trùng khớp"
                            } else {
                                val hashResult = PinManager.hashPin(pinInput)
                                coroutineScope.launch {
                                    appPreferences.savePin(hashResult.hashBase64, hashResult.saltBase64)
                                    showPinDialog = false
                                    // Tự động kích hoạt màn hình khóa để người dùng xác nhận
                                    onLockApp?.invoke(false)
                                }
                            }
                        }
                    ) {
                        Text("Lưu & Khóa ngay")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPinDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
