package com.billkeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.billkeeper.data.model.MonthlyStatistics
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

/**
 * 柱状图卡片 - 显示月度收支对比
 */
@Composable
fun BarChartCard(
    monthlyStatistics: List<MonthlyStatistics>,
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
                text = "月度对比",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (monthlyStatistics.isEmpty()) {
                Text(
                    text = "暂无数据",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    factory = { context ->
                        com.github.mikephil.charting.charts.BarChart(context).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            axisLeft.axisMinimum = 0f
                            axisRight.isEnabled = false
                            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            xAxis.granularity = 1f
                        }
                    },
                    update = { chart ->
                        val incomeEntries = monthlyStatistics.mapIndexed { index, stat ->
                            BarEntry(index.toFloat(), stat.income.toFloat())
                        }
                        val expenseEntries = monthlyStatistics.mapIndexed { index, stat ->
                            BarEntry(index.toFloat(), stat.expense.toFloat())
                        }

                        val incomeDataSet = BarDataSet(incomeEntries, "收入").apply {
                            color = Color(0xFF4CAF50).toArgb()
                            setDrawValues(false)
                        }

                        val expenseDataSet = BarDataSet(expenseEntries, "支出").apply {
                            color = Color(0xFFF44336).toArgb()
                            setDrawValues(false)
                        }

                        val groupSpace = 0.3f
                        val barSpace = 0.05f
                        val barWidth = (1f - groupSpace) / 2f - barSpace

                        val data = BarData(incomeDataSet, expenseDataSet).apply {
                            barWidth = barWidth
                        }

                        chart.data = data
                        chart.groupBars(0f, groupSpace, barSpace)

                        val months = monthlyStatistics.map { it.month }
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(months)

                        chart.animateY(1000)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}
