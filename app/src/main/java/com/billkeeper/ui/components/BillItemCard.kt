package com.billkeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.billkeeper.data.model.Bill
import com.billkeeper.data.model.PaymentPlatform
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BillItemCard(
    bill: Bill,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platform = try {
        PaymentPlatform.valueOf(bill.platform)
    } catch (_: Exception) {
        PaymentPlatform.MANUAL
    }
    val timeFormat = SimpleDateFormat("HH:mm", Locale.CHINA)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 支付平台图标
            PlatformIcon(
                platform = platform,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 账单信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.payee.ifEmpty { "未知收款方" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = platform.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (bill.payerAccount.isNotBlank()) {
                        Text(
                            text = " · ${bill.payerAccount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
                // 标签
                if (bill.tags.isNotBlank()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        bill.tags.split(",").take(3).forEach { tag ->
                            TagChip(tag = tag.trim())
                        }
                    }
                }
            }

            // 金额和时间
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "-¥%.2f".format(bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF44336)
                )
                Text(
                    text = timeFormat.format(Date(bill.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PlatformIcon(
    platform: PaymentPlatform,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, initial) = when (platform) {
        PaymentPlatform.WECHAT -> Triple(Color(0xFF07C160), Color.White, "微")
        PaymentPlatform.ALIPAY -> Triple(Color(0xFF1677FF), Color.White, "支")
        PaymentPlatform.MEITUAN -> Triple(Color(0xFFFFD100), Color.Black, "美")
        PaymentPlatform.JD -> Triple(Color(0xFFE4393C), Color.White, "京")
        PaymentPlatform.TAOBAO -> Triple(Color(0xFFFF6A00), Color.White, "淘")
        PaymentPlatform.BANK_CARD -> Triple(Color(0xFF78909C), Color.White, "卡")
        PaymentPlatform.MANUAL -> Triple(Color(0xFF9E9E9E), Color.White, "手")
    }

    Surface(
        modifier = modifier.clip(CircleShape),
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = initial,
                color = textColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
