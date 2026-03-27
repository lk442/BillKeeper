package com.billkeeper.ui.bill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.Bill
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository
) : ViewModel() {

    private val _bill = MutableStateFlow<Bill?>(null)
    val bill: StateFlow<Bill?> = _bill.asStateFlow()

    private val _editAmount = MutableStateFlow("0")
    val editAmount: StateFlow<String> = _editAmount.asStateFlow()

    private val _editPayee = MutableStateFlow("")
    val editPayee: StateFlow<String> = _editPayee.asStateFlow()

    private val _editPayerAccount = MutableStateFlow("")
    val editPayerAccount: StateFlow<String> = _editPayerAccount.asStateFlow()

    private val _editTags = MutableStateFlow<List<String>>(emptyList())
    val editTags: StateFlow<List<String>> = _editTags.asStateFlow()

    private val _editRemark = MutableStateFlow("")
    val editRemark: StateFlow<String> = _editRemark.asStateFlow()

    private val _newTagText = MutableStateFlow("")
    val newTagText: StateFlow<String> = _newTagText.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private var currentBillId: Long = 0

    fun loadBill(billId: Long) {
        currentBillId = billId
        viewModelScope.launch {
            val bill = billRepository.getBillById(billId)
            if (bill != null) {
                _bill.value = bill
                _editAmount.value = if (bill.amount == bill.amount.toLong().toDouble())
                    bill.amount.toLong().toString() else bill.amount.toString()
                _editPayee.value = bill.payee
                _editPayerAccount.value = bill.payerAccount
                _editTags.value = if (bill.tags.isBlank()) emptyList()
                else bill.tags.split(",").map { it.trim() }
                _editRemark.value = bill.remark
            }
        }
    }

    fun onAmountChanged(value: String) { _editAmount.value = value }
    fun onPayeeChanged(value: String) { _editPayee.value = value }
    fun onPayerAccountChanged(value: String) { _editPayerAccount.value = value }
    fun onRemarkChanged(value: String) { _editRemark.value = value }
    fun onNewTagTextChanged(value: String) { _newTagText.value = value }

    fun addTag() {
        val tag = _newTagText.value.trim()
        if (tag.isNotBlank() && tag !in _editTags.value) {
            _editTags.value = _editTags.value + tag
            _newTagText.value = ""
        }
    }

    fun removeTag(tag: String) {
        _editTags.value = _editTags.value - tag
    }

    fun saveBill() {
        val current = _bill.value ?: return
        viewModelScope.launch {
            billRepository.updateBill(
                current.copy(
                    amount = _editAmount.value.toDoubleOrNull() ?: 0.0,
                    payee = _editPayee.value,
                    payerAccount = _editPayerAccount.value,
                    tags = _editTags.value.joinToString(","),
                    remark = _editRemark.value
                )
            )
        }
    }

    fun showDeleteDialog() { _showDeleteDialog.value = true }
    fun dismissDeleteDialog() { _showDeleteDialog.value = false }

    fun deleteBill() {
        viewModelScope.launch {
            billRepository.deleteBill(currentBillId)
        }
    }
}
