package com.annhien.womenshealth.feature.settings

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.annhien.womenshealth.core.backup.BackupManager
import com.annhien.womenshealth.core.backup.BackupResult
import com.annhien.womenshealth.core.backup.RestoreResult
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    backupManager: BackupManager,
    context: Context,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var backupPassword by remember { mutableStateOf("") }
    var restorePassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sao lưu & Khôi phục", fontWeight = FontWeight.Bold) },
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

            if (statusMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 1. Export Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tạo bản sao lưu mã hóa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toàn bộ cơ sở dữ liệu chu kỳ, nhật ký, triệu chứng sẽ được đóng gói và mã hóa AES-256-GCM với mật khẩu do bạn tự đặt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = backupPassword,
                        onValueChange = { backupPassword = it },
                        label = { Text("Đặt mật khẩu bảo vệ file sao lưu") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (backupPassword.length < 4) {
                                statusMessage = "Mật khẩu sao lưu phải có ít nhất 4 ký tự."
                                return@Button
                            }
                            coroutineScope.launch {
                                isProcessing = true
                                val destFile = File(context.filesDir, "AnNhien_Backup_${LocalDate.now()}.annhien_backup")
                                val res = backupManager.createBackup(backupPassword.toCharArray(), destFile)
                                isProcessing = false
                                statusMessage = when (res) {
                                    is BackupResult.Success -> "Sao lưu thành công! Đã lưu tại: ${res.backupFile.name}"
                                    is BackupResult.Error -> res.cause
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isProcessing) "Đang tạo bản sao lưu..." else "Tạo tệp sao lưu (.annhien_backup)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Restore Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Khôi phục dữ liệu từ bản sao lưu",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Khôi phục dữ liệu đã sao lưu trước đó. Mật khẩu phải khớp chính xác với mật khẩu lúc tạo bản sao lưu.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = restorePassword,
                        onValueChange = { restorePassword = it },
                        label = { Text("Nhập mật khẩu giải mã") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val latestBackup = context.filesDir.listFiles { _, name -> name.endsWith(".annhien_backup") }?.lastOrNull()
                                if (latestBackup == null) {
                                    isProcessing = false
                                    statusMessage = "Không tìm thấy tệp sao lưu trong bộ nhớ ứng dụng."
                                    return@launch
                                }
                                val res = backupManager.restoreBackup(restorePassword.toCharArray(), latestBackup)
                                isProcessing = false
                                statusMessage = when (res) {
                                    is RestoreResult.Success -> res.message
                                    is RestoreResult.Error -> res.cause
                                }
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isProcessing) "Đang giải mã..." else "Xác nhận khôi phục")
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
