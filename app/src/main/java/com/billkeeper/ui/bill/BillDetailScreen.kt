package com.billkeeper.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.billkeeper.ui.components.PlatformIcon
import com.billkeeper.ui.components.TagChip
import com.billkeeper.ui.components.TagChipColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    billId: Long,
    onNavigateBack: () -> Unit,
    viewModel: BillDetailViewModel = hiltViewModel()
) {
    val bill by viewModel.bill.collectAsState()
    val editAmount by viewModel.editAmount.collectAsState()
    val editPayee by viewModel.editPayee.collectAsState()
    val editPayerAccount by viewModel.editPayerAccount.collectAsState()
    val editTags by viewModel.editTags.collectAsState()
    val editRemark by viewModel.editRemark.collectAsState()
    val newTagText by viewModel.newTagText.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    LaunchedEffect(billId) {
        viewModel.loadBill(billId)
    }

    LaunchedEffect(bill) {
        if (bill == null) {
            onNavigateBack()
        }
    }

    if (bill == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账单详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showDeleteDialog() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.saveBill(); onNavigateBack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("保存修改")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 支付平台和金额
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlatformIcon(
                        platform = try {
                            com.billkeeper.data.model.PaymentPlatform.valueOf(bill!!.platform)
                        } catch (_: Exception) {
                            com.billkeeper.data.model.PaymentPlatform.MANUAL
                        },
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = viewModel::onAmountChanged,
                        prefix = { Text("¥") },
                        modifier = Modifier.width(200.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 收款方
            EditableField(
                label = "收款方",
                value = editPayee,
                onValueChange = viewModel::onPayeeChanged,
                icon = Icons.Default.Store
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 付款账户
            EditableField(
                label = "付款账户",
                value = editPayerAccount,
                onValueChange = viewModel::onPayerAccountChanged,
                icon = Icons.Default.AccountBalanceWallet,
                placeholder = "如：微信零钱、招商银行"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 标签
            Text(
                text = "标签",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                editTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        color = TagChipColors(tag),
                        onRemove = { viewModel.removeTag(tag) }
                    )
                }
            }

            // 添加标签
            OutlinedTextField(
                value = newTagText,
                onValueChange = viewModel::onNewTagTextChanged,
                placeholder = { Text("添加标签") },
                trailingIcon = {
                    if (newTagText.isNotBlank()) {
                        IconButton(onClick = { viewModel.addTag() }) {
                            Icon(Icons.Default.Add, contentDescription = "添加")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 备注
            Text(
                text = "备注",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = editRemark,
                onValueChange = viewModel::onRemarkChanged,
                placeholder = { Text("添加备注...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // 删除确认弹窗
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("删除账单") },
            text = { Text("确定要删除这条账单吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBill()
                    onNavigateBack()
                }) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp)) },
        placeholder = if (placeholder.isNotEmpty()) {{ Text(placeholder) }} else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}
