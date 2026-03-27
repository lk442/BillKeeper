package com.billkeeper.ui.record

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.ViewModel
import com.billkeeper.data.model.Bill
import com.billkeeper.data.model.BillSource
import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecognizedBill(
    val amount: Double = 0.0,
    val payee: String = "",
    val payerAccount: String = "",
    val platform: PaymentPlatform = PaymentPlatform.MANUAL,
    val tags: String = "",
    val remark: String = ""
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val billRepository: BillRepository
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognizedBill = MutableStateFlow<RecognizedBill?>(null)
    val recognizedBill: StateFlow<RecognizedBill?> = _recognizedBill.asStateFlow()

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog: StateFlow<Boolean> = _showConfirmDialog.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun toggleRecording() {
        _isRecording.value = !_isRecording.value
        if (_isRecording.value) {
            _message.value = "开始录屏，请在支付App中完成支付"
        } else {
            _message.value = "录屏结束，正在分析..."
            // 录屏停止后会触发ScreenRecordService回调
        }
    }

    fun onBillRecognized(bill: RecognizedBill) {
        _recognizedBill.value = bill
        _showConfirmDialog.value = true
    }

    fun confirmBill() {
        val recognized = _recognizedBill.value ?: return
        viewModelScope.launch {
            val bill = Bill(
                amount = recognized.amount,
                payee = recognized.payee,
                payerAccount = recognized.payerAccount,
                platform = recognized.platform.name,
                tags = recognized.tags,
                remark = recognized.remark,
                source = when {
                    _isRecording.value -> BillSource.SCREEN_RECORD.name
                    else -> BillSource.SCREENSHOT.name
                }
            )
            billRepository.insertBill(bill)
            _showConfirmDialog.value = false
            _recognizedBill.value = null
            _message.value = "记账成功！"
        }
    }

    fun dismissDialog() {
        _showConfirmDialog.value = false
        _recognizedBill.value = null
    }

    fun clearMessage() {
        _message.value = null
    }

    // 检查悬浮窗权限
    fun hasOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    // 请求悬浮窗权限
    fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // 选择截图
    fun pickScreenshot() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // 由Activity处理ActivityResultLauncher
    }
}
