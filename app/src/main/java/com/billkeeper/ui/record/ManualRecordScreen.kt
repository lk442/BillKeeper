package com.billkeeper.ui.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billkeeper.data.model.IncomeType

/**
 * 手动记账页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualRecordScreen(
    viewModel: ManualRecordViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val amount by viewModel.amount.collectAsState()
    val incomeType by viewModel.incomeType.collectAsState()
    val date by viewModel.date.collectAsState()
    val category by viewModel.category.collectAsState()
    val remark by viewModel.remark.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("手动记账") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 金额输入
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (incomeType == IncomeType.EXPENSE) "支出金额" else "收入金额",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amount,
                        onValueChange = viewModel::onAmountChange,
                        placeholder = { Text("0.00") },
                        prefix = { Text("¥") },
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 收支类型切换
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = incomeType == IncomeType.EXPENSE,
                    onClick = { viewModel.onIncomeTypeChange(IncomeType.EXPENSE) },
                    label = { Text("支出") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = incomeType == IncomeType.INCOME,
                    onClick = { viewModel.onIncomeTypeChange(IncomeType.INCOME) },
                    label = { Text("收入") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 日期选择
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "日期",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 分类选择
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showCategoryDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (category.isEmpty()) "选择分类" else category,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (category.isEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }

            // 备注输入
            OutlinedTextField(
                value = remark,
                onValueChange = viewModel::onRemarkChange,
                placeholder = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            // 错误提示
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // 保存按钮
            Button(
                onClick = { viewModel.saveBill(onBack) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("保存", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    // 日期选择器
    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                viewModel.onDateChange(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    // 分类选择对话框
    if (showCategoryDialog) {
        CategoryDialog(
            categories = viewModel.defaultCategories,
            selectedCategory = category,
            onCategorySelected = { selected ->
                viewModel.onCategoryChange(selected)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

/**
 * 简单的日期选择器
 */
@Composable
fun DatePickerDialog(
    onDateSelected: (java.time.LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    DatePickerDialog(
        onDateSelected = { selectedDateMillis ->
            selectedDateMillis?.let {
                val date = java.time.Instant.ofEpochMilli(it)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                onDateSelected(date)
            }
        },
        onDismiss = onDismiss
    )
}

/**
 * 分类选择对话框
 */
@Composable
fun CategoryDialog(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择分类") },
        text = {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
