package com.billkeeper.ui.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.Bill
import com.billkeeper.data.model.BillSource
import com.billkeeper.data.model.IncomeType
import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * 手动记账ViewModel
 */
@HiltViewModel
class ManualRecordViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _incomeType = MutableStateFlow(IncomeType.EXPENSE)
    val incomeType: StateFlow<IncomeType> = _incomeType.asStateFlow()

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _remark = MutableStateFlow("")
    val remark: StateFlow<String> = _remark.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val defaultCategories = listOf(
        "餐饮", "交通", "购物", "娱乐", "日常", "医疗", "教育", "其他"
    )

    fun onAmountChange(newAmount: String) {
        _amount.value = newAmount
    }

    fun onIncomeTypeChange(newType: IncomeType) {
        _incomeType.value = newType
    }

    fun onDateChange(newDate: LocalDate) {
        _date.value = newDate
    }

    fun onCategoryChange(newCategory: String) {
        _category.value = newCategory
    }

    fun onRemarkChange(newRemark: String) {
        _remark.value = newRemark
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveBill(onSuccess: () -> Unit) {
        val amountStr = _amount.value.trim()
        if (amountStr.isEmpty()) {
            _errorMessage.value = "请输入金额"
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _errorMessage.value = "请输入有效的金额"
            return
        }

        if (_category.value.isEmpty()) {
            _errorMessage.value = "请选择分类"
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            try {
                val bill = Bill(
                    id = 0,
                    date = _date.value.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
                    amount = amount,
                    payerAccount = "手动记账",
                    payee = _category.value,
                    platform = PaymentPlatform.MANUAL.name,
                    tags = "",
                    remark = _remark.value,
                    workbookId = 1,
                    source = BillSource.MANUAL.name,
                    incomeType = _incomeType.value.name,
                    isDeleted = false
                )

                billRepository.insertBill(bill)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "保存失败：${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
}
