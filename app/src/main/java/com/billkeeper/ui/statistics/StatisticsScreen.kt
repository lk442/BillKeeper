package com.billkeeper.ui.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billkeeper.ui.components.BarChartCard
import com.billkeeper.ui.components.LineChartCard
import com.billkeeper.ui.components.PieChartCard

/**
 * 统计页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val statistics by viewModel.statistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                actions = {
                    // 时间选择器
                    var expanded by remember { mutableStateOf(false) }

                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Text(selectedPeriod.displayName)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "选择时间"
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            TimePeriod.values().forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period.displayName) },
                                    onClick = {
                                        viewModel.selectPeriod(period)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            statistics?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 统计卡片：总收入、总支出、结余
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "总收入",
                            amount = data.totalIncome,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "总支出",
                            amount = data.totalExpense,
                            color = MaterialTheme.colorScheme.error
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "结余",
                            amount = data.balance,
                            color = if (data.balance >= 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    // 饼图：支出分布
                    PieChartCard(
                        modifier = Modifier,
                        categoryStatistics = data.categoryStatistics
                    )

                    // 柱状图：月度对比
                    BarChartCard(
                        modifier = Modifier,
                        monthlyStatistics = data.monthlyStatistics
                    )

                    // 折线图：每日趋势
                    LineChartCard(
                        modifier = Modifier,
                        dailyStatistics = data.dailyStatistics
                    )

                    // 底部留白
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } ?: run {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无数据",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 统计数字卡片
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¥${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = color
            )
        }
    }
}
