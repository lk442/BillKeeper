package com.billkeeper.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.Bill
import com.billkeeper.data.model.PaymentPlatform
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BillViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword.asStateFlow()

    private val _selectedPlatform = MutableStateFlow<PaymentPlatform?>(null)
    val selectedPlatform: StateFlow<PaymentPlatform?> = _selectedPlatform.asStateFlow()

    val bills: StateFlow<List<Bill>> = combine(
        searchKeyword.debounce(300),
        _selectedPlatform
    ) { keyword, platform ->
        Pair(keyword, platform)
    }.flatMapLatest { (keyword, platform) ->
        when {
            keyword.isNotBlank() -> billRepository.searchBills(keyword = keyword)
            platform != null -> billRepository.getBillsByPlatform(platform = platform.name)
            else -> billRepository.getAllBills()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchKeywordChanged(keyword: String) {
        _searchKeyword.value = keyword
    }

    fun onPlatformSelected(platform: PaymentPlatform?) {
        _selectedPlatform.value = platform
    }

    fun deleteBill(billId: Long) {
        viewModelScope.launch {
            billRepository.deleteBill(billId)
        }
    }
}
