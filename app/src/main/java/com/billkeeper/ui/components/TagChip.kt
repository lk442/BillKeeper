package com.billkeeper.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TagChip(
    tag: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onLongClick = { onRemove?.invoke() }
        ),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Row {
            Text(
                text = tag,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            if (onRemove != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除标签",
                    modifier = Modifier
                        .padding(top = 6.dp, end = 6.dp)
                        .padding(start = 0.dp),
                    tint = color,
                )
            }
        }
    }
}

@Composable
fun TagChipColors(tag: String): Color {
    val colors = listOf(
        Color(0xFFFF7043), // 餐饮橙
        Color(0xFF42A5F5), // 交通蓝
        Color(0xFFAB47BC), // 购物紫
        Color(0xFFEC407A), // 娱乐粉
        Color(0xFF66BB6A), // 日常绿
        Color(0xFF78909C), // 其他灰
        Color(0xFFFFA726), // 日用品黄
        Color(0xFF5C6BC0), // 学习靛
    )
    return colors[tag.hashCode().mod(colors.size)]
}
