package com.billkeeper.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.billkeeper.data.model.Bill
import java.text.SimpleDateFormat
import java.util.*

/**
 * 小票样式卡片组件 - 参考阿柴记账的今日消费小票设计
 */
@Composable
fun ReceiptCard(
    bills: List<Bill>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 小票顶部 - 店铺信息风格
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    tint = Color(0xFF795548),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "今日消费小票",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 日期
            Text(
                text = SimpleDateFormat("yyyy-MM-dd EEEE", Locale.CHINA)
                    .format(Date()),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9E9E9E),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // 上虚线
            DashedLine()

            if (bills.isEmpty()) {
                // 无账单提示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "今天还没有消费记录~",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFBDBDBD),
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                // 账单列表
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bills.forEach { bill ->
                        ReceiptItemRow(bill = bill)
                    }
                }
            }

            // 下虚线
            DashedLine()

            // 合计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "合计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF795548),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "¥%.2f".format(totalAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 小票底部锯齿效果
            ReceiptSerratedEdge()
        }
    }
}

@Composable
private fun ReceiptItemRow(bill: Bill) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.payee.ifEmpty { "未知收款方" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF424242),
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
            if (bill.tags.isNotBlank()) {
                Text(
                    text = bill.tags.split(",").take(2).joinToString(" "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
        Text(
            text = timeFormat.format(Date(bill.date)),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9E9E9E),
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "-¥%.2f".format(bill.amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun DashedLine(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(vertical = 8.dp)
    ) {
        val strokeWidth = 1.dp.toPx()
        val dashWidth = 8.dp.toPx()
        val gapWidth = 4.dp.toPx()
        var x = 0f

        while (x < size.width) {
            drawLine(
                color = Color(0xFFBDBDBD),
                start = Offset(x, size.height / 2),
                end = Offset(
                    x = (x + dashWidth).coerceAtMost(size.width),
                    y = size.height / 2
                ),
                strokeWidth = strokeWidth
            )
            x += dashWidth + gapWidth
        }
    }
}

@Composable
private fun ReceiptSerratedEdge() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        val triangleWidth = 12.dp.toPx()
        val triangleHeight = 8.dp.toPx()
        val color = Color(0xFFFFF8E1)

        var x = 0f
        while (x < size.width) {
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x, 0f)
                    lineTo(x + triangleWidth / 2, triangleHeight)
                    lineTo(x + triangleWidth, 0f)
                    close()
                },
                color = color
            )
            x += triangleWidth
        }
    }
}
