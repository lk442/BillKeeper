package com.billkeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.billkeeper.data.model.CategoryStatistics
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

/**
 * 饼图卡片 - 显示支出分类分布
 */
@Composable
fun PieChartCard(
    categoryStatistics: List<CategoryStatistics>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "支出分布",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (categoryStatistics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    factory = { context ->
                        com.github.mikephil.charting.charts.PieChart(context).apply {
                            description.isEnabled = false
                            legend.isEnabled = true
                            setHoleRadius(40f)
                            setTransparentCircleRadius(45f)
                            setDrawCenterText(true)
                            centerText = "支出"
                        }
                    },
                    update = { chart ->
                        val entries = categoryStatistics.map { (category, amount) ->
                            PieEntry(amount.toFloat(), category)
                        }

                        val colors = listOf(
                            Color(0xFFFF7043).toArgb(), // 橙红
                            Color(0xFF42A5F5).toArgb(), // 蓝
                            Color(0xFFAB47BC).toArgb(), // 紫
                            Color(0xFFEC407A).toArgb(), // 粉
                            Color(0xFF66BB6A).toArgb(), // 绿
                            Color(0xFF78909C).toArgb(), // 灰
                            Color(0xFFFFA726).toArgb(), // 橙
                            Color(0xFF26A69A).toArgb()  // 青
                        )

                        val dataSet = PieDataSet(entries, "支出").apply {
                            this.colors = colors
                            setDrawValues(true)
                            valueTextSize = 12f
                            valueFormatter = PercentFormatter(chart)
                        }

                        chart.data = PieData(dataSet).apply {
                            setValueTextSize(12f)
                        }
                        chart.invalidate()
                    }
                )
            }
        }
    }
}
