package com.billkeeper.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.billkeeper.data.model.StatisticsData
import com.billkeeper.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 统计页面ViewModel
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val billRepository: BillRepository
) : ViewModel() {

    private val _statistics = MutableStateFlow<StatisticsData?>(null)
    val statistics: StateFlow<StatisticsData?> = _statistics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TimePeriod.THIS_MONTH)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    init {
        loadStatistics()
    }

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (startTime, endTime) = getTimeRange(_selectedPeriod.value)
                val data = billRepository.getStatistics(
                    workbookId = 1,
                    startTime = startTime,
                    endTime = endTime
                )
                _statistics.value = data
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun getTimeRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (period) {
            TimePeriod.THIS_MONTH -> {
                val start = calendar.clone() as Calendar
                start.set(Calendar.DAY_OF_MONTH, 1)
                val end = calendar.clone() as Calendar
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                Pair(start.timeInMillis, end.timeInMillis)
            }
            TimePeriod.LAST_MONTH -> {
                val start = calendar.clone() as Calendar
                start.add(Calendar.MONTH, -1)
                start.set(Calendar.DAY_OF_MONTH, 1)
                val end = calendar.clone() as Calendar
                end.add(Calendar.MONTH, -1)
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                Pair(start.timeInMillis, end.timeInMillis)
            }
            TimePeriod.THIS_YEAR -> {
                val start = calendar.clone() as Calendar
                start.set(Calendar.MONTH, Calendar.JANUARY)
                start.set(Calendar.DAY_OF_MONTH, 1)
                val end = calendar.clone() as Calendar
                end.set(Calendar.MONTH, Calendar.DECEMBER)
                end.set(Calendar.DAY_OF_MONTH, 31)
                end.set(Calendar.HOUR_OF_DAY, 23)
                end.set(Calendar.MINUTE, 59)
                end.set(Calendar.SECOND, 59)
                Pair(start.timeInMillis, end.timeInMillis)
            }
        }
    }
}

/**
 * 时间周期枚举
 */
enum class TimePeriod(val displayName: String) {
    THIS_MONTH("本月"),
    LAST_MONTH("上月"),
    THIS_YEAR("今年")
}
