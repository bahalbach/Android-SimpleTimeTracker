package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.mapper.RangeMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.feature_views.spinner.CustomSpinner
import com.example.util.simpletimetracker.core.viewData.RangeViewData
import com.example.util.simpletimetracker.core.viewData.RangesViewData
import com.example.util.simpletimetracker.core.viewData.SelectDateViewData
import com.example.util.simpletimetracker.core.viewData.SelectRangeViewData
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.Range
import com.example.util.simpletimetracker.domain.model.RangeLength
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.CustomRangeSelectionParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.launch
import javax.inject.Inject

class StatisticsContainerViewModel @Inject constructor(
    private val router: Router,
    private val timeMapper: TimeMapper,
    private val rangeMapper: RangeMapper,
    private val prefsInteractor: PrefsInteractor,
) : ViewModel() {

    val title: LiveData<String> by lazy {
        return@lazy MutableLiveData<String>().let { initial ->
            viewModelScope.launch { initial.value = loadTitle() }
            initial
        }
    }

    val position: LiveData<Int> by lazy {
        return@lazy MutableLiveData(0)
    }

    val rangeItems: LiveData<RangesViewData> by lazy {
        return@lazy MutableLiveData(loadRanges())
    }

    private var rangeLength: RangeLength = RangeLength.Day

    fun onVisible() {
        updateTitle()
    }

    fun onPreviousClick() {
        updatePosition(position.value.orZero() - 1)
    }

    fun onTodayClick() {
        updatePosition(0)
    }

    fun onNextClick() {
        updatePosition(position.value.orZero() + 1)
    }

    fun onRangeSelected(item: CustomSpinner.CustomSpinnerItem) {
        when (item) {
            is SelectDateViewData -> {
                onSelectDateClick()
                updateRanges()
            }
            is SelectRangeViewData -> {
                onSelectRangeClick()
                updateRanges()
            }
            is RangeViewData -> {
                rangeLength = item.range
                updatePosition(0)
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            DATE_TAG -> {
                timeMapper.toTimestampShift(
                    toTime = timestamp,
                    range = rangeLength,
                    firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
                ).toInt().let(::updatePosition)
            }
        }
    }

    fun onCustomRangeSelected(range: Range) {
        rangeLength = RangeLength.Custom(range)
        updatePosition(0)
    }

    private fun onSelectDateClick() = viewModelScope.launch {
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        val current = timeMapper.toTimestampShifted(
            rangesFromToday = position.value.orZero(),
            range = rangeLength
        )

        router.navigate(
            DateTimeDialogParams(
                tag = DATE_TAG,
                type = DateTimeDialogType.DATE,
                timestamp = current,
                useMilitaryTime = useMilitaryTime,
                firstDayOfWeek = firstDayOfWeek
            )
        )
    }

    private fun onSelectRangeClick() = viewModelScope.launch {
        val currentCustomRange = (rangeLength as? RangeLength.Custom)?.range

        CustomRangeSelectionParams(
            rangeStart = currentCustomRange?.timeStarted,
            rangeEnd = currentCustomRange?.timeEnded,
        ).let(router::navigate)
    }

    private fun updatePosition(newPosition: Int) {
        (position as MutableLiveData).value = newPosition
        updateTitle()
        updateRanges()
    }

    private fun updateTitle() = viewModelScope.launch {
        (title as MutableLiveData).value = loadTitle()
    }

    private suspend fun loadTitle(): String {
        val firstDayOfWeek = prefsInteractor.getFirstDayOfWeek()
        return rangeMapper.mapToTitle(rangeLength, position.value.orZero(), firstDayOfWeek)
    }

    private fun updateRanges() {
        (rangeItems as MutableLiveData).value = loadRanges()
    }

    private fun loadRanges(): RangesViewData {
        return rangeMapper.mapToRanges(rangeLength)
    }

    companion object {
        private const val DATE_TAG = "statistics_date_tag"
    }
}
