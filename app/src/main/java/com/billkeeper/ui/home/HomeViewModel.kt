package com.billkeeper.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.Bill
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val calendar = Calendar.getInstance()

    // 当月起始和结束时间
    private val monthStart: Long
    private val monthEnd: Long

    // 今日起始和结束时间
    val todayStart: Long
    val todayEnd: Long

    init {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        monthStart = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        monthEnd = calendar.timeInMillis

        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        todayStart = today.timeInMillis

        today.set(Calendar.HOUR_OF_DAY, 23)
        today.set(Calendar.MINUTE, 59)
        today.set(Calendar.SECOND, 59)
        todayEnd = today.timeInMillis
    }

    val todayBills: StateFlow<List<Bill>> = billRepository
        .getTodayBills(startOfDay = todayStart, endOfDay = todayEnd)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Double> = flow {
        emit(billRepository.getTodayTotal(startOfDay = todayStart, endOfDay = todayEnd))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthTotal: StateFlow<Double> = flow {
        emit(billRepository.getTotalExpense(startTime = monthStart, endTime = monthEnd))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _currentMonth = MutableStateFlow(
        "${calendar.get(Calendar.YEAR)}年${calendar.get(Calendar.MONTH) + 1}月"
    )
    val currentMonth: StateFlow<String> = _currentMonth.asStateFlow()
}
