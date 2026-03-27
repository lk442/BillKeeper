package com.billkeeper.ui.record

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBillDetail: (Long) -> Unit,
    onNavigateToManualRecord: () -> Unit = {},
    viewModel: RecordViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val showConfirmDialog by viewModel.showConfirmDialog.collectAsState()
    val recognizedBill by viewModel.recognizedBill.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(message) {
        if (message != null) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记一笔") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // 中央按钮
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(200.dp)
                ) {
                    // 外圈动画
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulse by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )

                    Canvas(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(if (isRecording) pulse else 1f)
                    ) {
                        drawCircle(
                            color = if (isRecording) Color(0xFFFF5252) else Color(0xFF4CAF50),
                            radius = size.minDimension / 2 - 4.dp.toPx(),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // 中央图标
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Add,
                        contentDescription = if (isRecording) "停止" else "记一笔",
                        tint = if (isRecording) Color.White else MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                if (isRecording) Color(0xFFFF5252)
                                else Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            )
                            .padding(24.dp)
                            .clickable {
                                if (isRecording) viewModel.toggleRecording()
                            }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // 记录状态
                Text(
                    text = when {
                        isRecording -> "正在录屏中..."
                        isRecognizing -> "正在识别中..."
                        else -> "选择记账方式"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        isRecording -> Color(0xFFFF5252)
                        isRecognizing -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(48.dp))

                // 四种记账方式入口
                RecordMethodGrid(
                    onFloatingWindow = {
                        if (viewModel.hasOverlayPermission()) {
                            // 启动悬浮窗服务
                        } else {
                            viewModel.requestOverlayPermission()
                        }
                    },
                    onScreenRecord = { viewModel.toggleRecording() },
                    onScreenshot = { viewModel.pickScreenshot() },
                    onManual = {
                        // 跳转手动记账
                    }
                )
            }

            // 消息提示
            message?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }

    // 确认账单弹窗
    if (showConfirmDialog && recognizedBill != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialog() },
            title = { Text("确认账单信息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoRow("付款金额", "¥%.2f".format(recognizedBill!!.amount))
                    InfoRow("收款方", recognizedBill!!.payee)
                    InfoRow("付款账户", recognizedBill!!.payerAccount)
                    InfoRow("支付平台", recognizedBill!!.platform.displayName)
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmBill() }) {
                    Text("添加到账单")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialog() }) {
                    Text("丢弃")
                }
            }
        )
    }
}

@Composable
private fun RecordMethodGrid(
    onFloatingWindow: () -> Unit,
    onScreenRecord: () -> Unit,
    onScreenshot: () -> Unit,
    onManual: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecordMethodItem(
                icon = Icons.Default.PictureInPicture,
                title = "悬浮窗记账",
                subtitle = "截屏识别",
                color = Color(0xFF4CAF50),
                onClick = onFloatingWindow,
                modifier = Modifier.weight(1f)
            )
            RecordMethodItem(
                icon = Icons.Default.Videocam,
                title = "录屏记账",
                subtitle = "录制支付过程",
                color = Color(0xFFFF7043),
                onClick = onScreenRecord,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecordMethodItem(
                icon = Icons.Default.PhotoLibrary,
                title = "截图导入",
                subtitle = "选择截图识别",
                color = Color(0xFF42A5F5),
                onClick = onScreenshot,
                modifier = Modifier.weight(1f)
            )
            RecordMethodItem(
                icon = Icons.Default.Edit,
                title = "手动记账",
                subtitle = "手动输入信息",
                color = Color(0xFFAB47BC),
                onClick = onManual,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecordMethodItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
