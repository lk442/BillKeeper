package com.billkeeper.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.billkeeper.data.model.DailyStatistics
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * 折线图卡片 - 显示每日支出趋势
 */
@Composable
fun LineChartCard(
    dailyStatistics: List<DailyStatistics>,
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
                text = "每日趋势",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (dailyStatistics.isEmpty()) {
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
                        com.github.mikephil.charting.charts.LineChart(context).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            axisLeft.axisMinimum = 0f
                            axisRight.isEnabled = false
                            xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                            axisLeft.setDrawGridLines(true)
                            xAxis.setDrawGridLines(false)
                            setTouchEnabled(true)
                            isDragEnabled = true
                            setScaleEnabled(true)
                        }
                    },
                    update = { chart ->
                        val entries = dailyStatistics.map { stat ->
                            Entry(
                                stat.dayTimestamp.toFloat(),
                                stat.expense.toFloat()
                            )
                        }

                        val dataSet = LineDataSet(entries, "每日支出").apply {
                            color = Color(0xFF4CAF50).toArgb()
                            setCircleColor(Color(0xFF4CAF50).toArgb())
                            lineWidth = 2f
                            circleRadius = 4f
                            setDrawFilled(true)
                            fillColor = Color(0x664CAF50).toArgb()
                            setDrawValues(false)
                            mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
                        }

                        chart.data = LineData(dataSet)

                        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                        val dates = dailyStatistics.map { stat ->
                            dateFormat.format(Date(stat.dayTimestamp * 1000))
                        }
                        chart.xAxis.valueFormatter = IndexAxisValueFormatter(dates)

                        chart.animateX(1000)
                        chart.invalidate()
                    }
                )
            }
        }
    }
}
