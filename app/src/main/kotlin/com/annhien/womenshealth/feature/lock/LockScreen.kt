package com.annhien.womenshealth.feature.lock

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.annhien.womenshealth.core.design.*

@Composable
fun LockScreen(
    title: String = "Khóa An Nhiên",
    subtitle: String = "Bảo vệ thông tin sức khỏe của bạn",
    expectedPinHash: String?,
    saltBase64: String?,
    startInCalculatorMode: Boolean = false,
    onUnlocked: () -> Unit,
    onBiometricRequested: (() -> Unit)? = null,
    isBiometricAvailable: Boolean = false
) {
    var isCalculatorMode by remember(startInCalculatorMode) { mutableStateOf(startInCalculatorMode) }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(startInCalculatorMode) {
        if (startInCalculatorMode) {
            isCalculatorMode = true
        }
    }

    // Calculator disguise state
    var calcDisplay by remember { mutableStateOf("0") }
    var calcExpression by remember { mutableStateOf("") }

    fun handleDigit(digit: String) {
        if (enteredPin.length < 6) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (expectedPinHash != null && saltBase64 != null) {
                    if (com.annhien.womenshealth.core.security.PinManager.verifyPin(newPin, saltBase64, expectedPinHash)) {
                        onUnlocked()
                    } else {
                        errorMessage = "Mã PIN không chính xác"
                        enteredPin = ""
                    }
                } else {
                    // Mặc định ban đầu nếu chưa đặt PIN: 1234
                    if (newPin == "1234") {
                        onUnlocked()
                    } else {
                        errorMessage = "Mã PIN không chính xác (Mặc định: 1234)"
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    fun handleCalcKey(key: String) {
        when (key) {
            "C" -> {
                calcDisplay = "0"
                calcExpression = ""
            }
            "+/-" -> {
                if (calcDisplay != "0") {
                    calcDisplay = if (calcDisplay.startsWith("-")) calcDisplay.drop(1) else "-$calcDisplay"
                    calcExpression = calcDisplay
                }
            }
            "%" -> {
                try {
                    val num = calcDisplay.toDouble() / 100.0
                    calcDisplay = if (num % 1.0 == 0.0) num.toLong().toString() else num.toString()
                    calcExpression = calcDisplay
                } catch (e: Exception) {
                    calcDisplay = "0"
                }
            }
            "=" -> {
                // Kiểm tra xem số đang nhập/hiển thị có phải mã PIN mở khóa không
                val isMatch = if (expectedPinHash != null && saltBase64 != null) {
                    com.annhien.womenshealth.core.security.PinManager.verifyPin(calcDisplay, saltBase64, expectedPinHash) ||
                    com.annhien.womenshealth.core.security.PinManager.verifyPin(calcExpression, saltBase64, expectedPinHash)
                } else {
                    calcDisplay == "1234" || calcExpression == "1234"
                }

                if (isMatch) {
                    onUnlocked()
                    return
                }

                // Nếu không phải PIN, tính toán biểu thức như máy tính thật
                try {
                    val result = evaluateSimpleMath(calcExpression)
                    calcDisplay = result
                    calcExpression = result
                } catch (e: Exception) {
                    calcDisplay = "0"
                }
            }
            "+", "-", "×", "÷" -> {
                calcExpression = if (calcExpression.isNotEmpty()) "$calcExpression$key" else "${calcDisplay}$key"
                calcDisplay = "0"
            }
            else -> {
                if (calcDisplay == "0") calcDisplay = key else calcDisplay += key
                calcExpression += key
            }
        }
    }

    if (isCalculatorMode) {
        // GIAO DIỆN MÁY TÍNH NGỤY TRANG
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E))
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isCalculatorMode = false }) {
                    Icon(Icons.Default.Lock, contentDescription = "Về màn hình PIN", tint = Color.Gray)
                }
                Text("Máy tính", color = Color.Gray, fontSize = 14.sp)
            }

            // Display
            Column(
                modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = calcDisplay,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    maxLines = 1
                )
            }

            // Keypad Calculator
            val calcKeys = listOf(
                listOf("C", "+/-", "%", "÷"),
                listOf("7", "8", "9", "×"),
                listOf("4", "5", "6", "-"),
                listOf("1", "2", "3", "+"),
                listOf("0", ".", "=")
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                calcKeys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.forEach { k ->
                            val isOp = k in listOf("÷", "×", "-", "+", "=")
                            val isTop = k in listOf("C", "+/-", "%")
                            val btnColor = when {
                                isOp -> Color(0xFFFF9F0A)
                                isTop -> Color(0xFFA5A5A5)
                                else -> Color(0xFF333333)
                            }
                            val txtColor = if (isTop) Color.Black else Color.White
                            val width = if (k == "0") 160.dp else 72.dp

                            Box(
                                modifier = Modifier
                                    .size(width = width, height = 72.dp)
                                    .clip(CircleShape)
                                    .background(btnColor)
                                    .clickable { handleCalcKey(k) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = k, fontSize = 28.sp, fontWeight = FontWeight.Medium, color = txtColor)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // GIAO DIỆN KHÓA PIN CHUẨN
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Nút chuyển chế độ ngụy trang máy tính ở góc trên
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { isCalculatorMode = true }) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Chế độ ngụy trang máy tính",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Keypad (1 to 9, Biometric / Empty, 0, Backspace)
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )

                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { digit ->
                            PinKeyButton(text = digit, onClick = { handleDigit(digit) })
                        }
                    }
                }

                // Bottom row: Biometric or empty, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left slot: Biometric button
                    if (isBiometricAvailable && onBiometricRequested != null) {
                        IconButton(
                            onClick = onBiometricRequested,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Sinh trắc học",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(72.dp))
                    }

                    // Center slot: 0
                    PinKeyButton(text = "0", onClick = { handleDigit("0") })

                    // Right slot: Backspace
                    IconButton(
                        onClick = { handleDelete() },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "Xóa",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun evaluateSimpleMath(expr: String): String {
    return try {
        val sanitized = expr.replace("×", "*").replace("÷", "/")
        val opIndex = sanitized.indexOfAny(charArrayOf('+', '-', '*', '/'), startIndex = 1)
        if (opIndex > 0) {
            val a = sanitized.substring(0, opIndex).trim().toDouble()
            val op = sanitized[opIndex]
            val b = sanitized.substring(opIndex + 1).trim().toDouble()
            val res = when (op) {
                '+' -> a + b
                '-' -> a - b
                '*' -> a * b
                '/' -> if (b != 0.0) a / b else Double.NaN
                else -> a
            }
            if (res.isNaN()) "Error"
            else if (res % 1.0 == 0.0) res.toLong().toString()
            else String.format(java.util.Locale.US, "%.4f", res).trimEnd('0').trimEnd('.')
        } else {
            expr
        }
    } catch (e: Exception) {
        "0"
    }
}

@Composable
private fun PinKeyButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
