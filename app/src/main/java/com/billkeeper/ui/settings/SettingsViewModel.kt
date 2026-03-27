package com.billkeeper.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.Workbook
import com.billkeeper.data.repository.BillRepository
import com.billkeeper.data.repository.WorkbookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billRepository: BillRepository,
    private val workbookRepository: WorkbookRepository
) : ViewModel() {

    private val _hasOverlayPermission = MutableStateFlow(false)
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasAccessibilityPermission = MutableStateFlow(false)
    val hasAccessibilityPermission: StateFlow<Boolean> = _hasAccessibilityPermission.asStateFlow()

    private val _hasStoragePermission = MutableStateFlow(false)
    val hasStoragePermission: StateFlow<Boolean> = _hasStoragePermission.asStateFlow()

    val workbooks = workbookRepository.getAllWorkbooks()

    private val _showCreateWorkbookDialog = MutableStateFlow(false)
    val showCreateWorkbookDialog: StateFlow<Boolean> = _showCreateWorkbookDialog.asStateFlow()

    private val _newWorkbookName = MutableStateFlow("")
    val newWorkbookName: StateFlow<String> = _newWorkbookName.asStateFlow()

    private val _showExportSuccess = MutableStateFlow<String?>(null)
    val showExportSuccess: StateFlow<String?> = _showExportSuccess.asStateFlow()

    fun checkPermissions() {
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
        _hasAccessibilityPermission.value = isAccessibilityServiceEnabled()
        _hasStoragePermission.value = checkStoragePermission()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? android.view.accessibility.AccessibilityManager
        val enabledServices = am?.getEnabledAccessibilityServiceList(
            android.view.accessibility.AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices?.any {
            it.resolveInfo.serviceInfo.packageName == context.packageName
        } ?: false
    }

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    fun showCreateWorkbookDialog() { _showCreateWorkbookDialog.value = true }
    fun dismissCreateWorkbookDialog() {
        _showCreateWorkbookDialog.value = false
        _newWorkbookName.value = ""
    }
    fun onNewWorkbookNameChanged(name: String) { _newWorkbookName.value = name }

    fun createWorkbook() {
        val name = _newWorkbookName.value.trim()
        if (name.isNotBlank()) {
            viewModelScope.launch {
                workbookRepository.createWorkbook(name)
                dismissCreateWorkbookDialog()
            }
        }
    }

    fun setDefaultWorkbook(id: Long) {
        viewModelScope.launch {
            workbookRepository.setDefaultWorkbook(id)
        }
    }

    fun exportExcel() {
        viewModelScope.launch {
            try {
                val file = billRepository.exportToExcel()
                _showExportSuccess.value = "导出成功：${file.name}"
            } catch (_: Exception) {
                _showExportSuccess.value = "导出失败，请检查存储权限"
            }
        }
    }

    fun clearExportMessage() { _showExportSuccess.value = null }
}
