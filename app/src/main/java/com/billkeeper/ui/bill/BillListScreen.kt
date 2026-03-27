package com.billkeeper.ui.bill

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.ui.components.BillFilterBar
import com.billkeeper.ui.components.BillItemCard
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBillDetail: (Long) -> Unit,
    viewModel: BillViewModel = hiltViewModel()
) {
    val bills by viewModel.bills.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()

    // 按日期分组
    val groupedBills = bills.groupBy { bill ->
        SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(bill.date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账单明细") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchKeyword,
                onValueChange = viewModel::onSearchKeywordChanged,
                placeholder = { Text("搜索收款方、金额、备注...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.large,
                singleLine = true
            )

            // 筛选栏
            BillFilterBar(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = viewModel::onPlatformSelected,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 账单列表
            if (bills.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无账单记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedBills.forEach { (date, dateBills) ->
                        item {
                            // 日期头
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDateDisplay(date),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "¥%.2f".format(dateBills.sumOf { it.amount }),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        items(dateBills, key = { it.id }) { bill ->
                            BillItemCard(
                                bill = bill,
                                onClick = { onNavigateToBillDetail(bill.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateDisplay(dateStr: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(dateStr)
        val calendar = Calendar.getInstance()
        calendar.time = date

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }

        when {
            isSameDay(calendar, today) -> "今天"
            isSameDay(calendar, yesterday) -> "昨天"
            else -> SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(date!!)
        }
    } catch (_: Exception) {
        dateStr
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
