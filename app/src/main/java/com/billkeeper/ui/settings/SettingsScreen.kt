package com.billkeeper.ui.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsState()
    val hasAccessibilityPermission by viewModel.hasAccessibilityPermission.collectAsState()
    val hasStoragePermission by viewModel.hasStoragePermission.collectAsState()
    val workbooks by viewModel.workbooks.collectAsState()
    val showCreateWorkbookDialog by viewModel.showCreateWorkbookDialog.collectAsState()
    val showExportSuccess by viewModel.showExportSuccess.collectAsState()

    LaunchedEffect(showExportSuccess) {
        if (showExportSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearExportMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 权限管理
            Text(
                text = "权限管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 悬浮窗权限
            SettingItem(
                icon = Icons.Default.PictureInPicture,
                title = "悬浮窗权限",
                subtitle = "用于在支付页面悬浮截屏",
                isGranted = hasOverlayPermission,
                onClick = {
                    if (!hasOverlayPermission) {
                        context.startActivity(Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ))
                    }
                }
            )

            // 无障碍服务
            SettingItem(
                icon = Icons.Default.Accessibility,
                title = "无障碍服务",
                subtitle = "用于自动识别支付通知",
                isGranted = hasAccessibilityPermission,
                onClick = {
                    if (!hasAccessibilityPermission) {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                }
            )

            // 存储权限
            SettingItem(
                icon = Icons.Default.Storage,
                title = "存储权限",
                subtitle = "用于导出Excel文件",
                isGranted = hasStoragePermission,
                onClick = {
                    if (!hasStoragePermission) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 账本管理
            SectionHeader(title = "账本管理", action = {
                TextButton(onClick = { viewModel.showCreateWorkbookDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("新建")
                }
            })

            workbooks.forEach { workbook ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { viewModel.setDefaultWorkbook(workbook.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = workbook.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (workbook.isDefault) {
                                Text(
                                    text = "默认",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 数据管理
            SectionHeader(title = "数据管理")

            SettingItem(
                icon = Icons.Default.FileDownload,
                title = "导出Excel",
                subtitle = "将账单数据导出为Excel文件",
                onClick = { viewModel.exportExcel() }
            )

            SettingItem(
                icon = Icons.Default.FileUpload,
                title = "导入账单",
                subtitle = "从Excel文件导入账单数据",
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 关于
            SectionHeader(title = "关于")
            SettingItem(
                icon = Icons.Default.Info,
                title = "版本信息",
                subtitle = "记账本 v1.0.0",
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // 新建账本弹窗
    if (showCreateWorkbookDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateWorkbookDialog() },
            title = { Text("新建账本") },
            text = {
                OutlinedTextField(
                    value = viewModel.newWorkbookName.value,
                    onValueChange = viewModel::onNewWorkbookNameChanged,
                    placeholder = { Text("请输入账本名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.createWorkbook() }) {
                    Text("创建")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateWorkbookDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    // 导出成功提示
    showExportSuccess?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(msg, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        action?.invoke()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isGranted != null) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    contentDescription = if (isGranted) "已开启" else "未开启",
                    tint = if (isGranted) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
