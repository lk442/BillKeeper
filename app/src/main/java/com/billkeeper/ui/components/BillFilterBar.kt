package com.billkeeper.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.billkeeper.data.model.PaymentPlatform

@Composable
fun BillFilterBar(
    selectedPlatform: PaymentPlatform?,
    onPlatformSelected: (PaymentPlatform?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 全部筛选
        FilterChip(
            selected = selectedPlatform == null,
            onClick = { onPlatformSelected(null) },
            label = { Text("全部") },
            shape = RoundedCornerShape(20.dp)
        )

        // 各支付平台筛选
        PaymentPlatform.entries.forEach { platform ->
            FilterChip(
                selected = selectedPlatform == platform,
                onClick = { onPlatformSelected(platform) },
                label = { Text(platform.displayName) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
