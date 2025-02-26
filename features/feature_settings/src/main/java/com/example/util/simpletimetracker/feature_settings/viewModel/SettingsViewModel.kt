package com.example.util.simpletimetracker.feature_settings.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.provider.PackageNameProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.WidgetType
import com.example.util.simpletimetracker.feature_settings.R
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.viewData.CardOrderViewData
import com.example.util.simpletimetracker.feature_settings.viewData.FirstDayOfWeekViewData
import com.example.util.simpletimetracker.feature_settings.viewData.SettingsStartOfDayViewData
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ArchiveParams
import com.example.util.simpletimetracker.navigation.params.screen.CardOrderDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CardSizeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.CategoriesParams
import com.example.util.simpletimetracker.navigation.params.screen.CsvExportSettingDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DurationDialogParams
import com.example.util.simpletimetracker.navigation.params.action.OpenMarketParams
import com.example.util.simpletimetracker.navigation.params.action.SendEmailParams
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogParams
import com.example.util.simpletimetracker.navigation.params.screen.DateTimeDialogType
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val router: Router,
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val settingsMapper: SettingsMapper,
    private val packageNameProvider: PackageNameProvider,
    private val notificationTypeInteractor: NotificationTypeInteractor,
    private val widgetInteractor: WidgetInteractor,
    private val notificationInactivityInteractor: NotificationInactivityInteractor,
) : ViewModel() {

    val cardOrderViewData: LiveData<CardOrderViewData> by lazy {
        MutableLiveData<CardOrderViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadCardOrderViewData()
            }
            initial
        }
    }

    val btnCardOrderManualVisibility: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getCardOrder() == CardOrder.MANUAL
            }
            initial
        }
    }

    val firstDayOfWeekViewData: LiveData<FirstDayOfWeekViewData> by lazy {
        MutableLiveData<FirstDayOfWeekViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadFirstDayOfWeekViewData()
            }
            initial
        }
    }

    val showUntrackedCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowUntrackedInRecords()
            }
            initial
        }
    }

    val allowMultitaskingCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getAllowMultitasking()
            }
            initial
        }
    }

    val showNotificationsCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowNotifications()
            }
            initial
        }
    }

    val startOfDayViewData: LiveData<SettingsStartOfDayViewData> by lazy {
        MutableLiveData<SettingsStartOfDayViewData>().let { initial ->
            viewModelScope.launch {
                initial.value = loadStartOfDayViewData()
            }
            initial
        }
    }

    val inactivityReminderViewData: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadInactivityReminderViewData()
            }
            initial
        }
    }

    val darkModeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getDarkMode()
            }
            initial
        }
    }

    val showRecordTagSelectionCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getShowRecordTagSelection()
            }
            initial
        }
    }

    val recordTagSelectionCloseCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getRecordTagSelectionCloseAfterOne()
            }
            initial
        }
    }

    val useMilitaryTimeCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getUseMilitaryTimeFormat()
            }
            initial
        }
    }

    val useMilitaryTimeHint: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUseMilitaryTimeViewData()
            }
            initial
        }
    }

    val useProportionalMinutesCheckbox: LiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().let { initial ->
            viewModelScope.launch {
                initial.value = prefsInteractor.getUseProportionalMinutes()
            }
            initial
        }
    }

    val useProportionalMinutesHint: LiveData<String> by lazy {
        MutableLiveData<String>().let { initial ->
            viewModelScope.launch {
                initial.value = loadUseProportionalMinutesViewData()
            }
            initial
        }
    }

    val themeChanged: LiveData<Boolean> = MutableLiveData(false)

    fun onVisible() {
        // Need to update card order because it changes on card order dialog
        viewModelScope.launch {
            updateCardOrderViewData()
        }
    }

    fun onExportCsvClick() {
        router.navigate(CsvExportSettingDialogParams)
    }

    fun onRateClick() {
        router.execute(OpenMarketParams(packageName = packageNameProvider.getPackageName()))
    }

    fun onFeedbackClick() {
        router.execute(
            data = SendEmailParams(
                email = resourceRepo.getString(R.string.support_email),
                subject = resourceRepo.getString(R.string.support_email_subject),
                chooserTitle = resourceRepo.getString(R.string.settings_email_chooser_title),
                notHandledCallback = { R.string.message_app_not_found.let(::showMessage) }
            )
        )
    }

    fun onRecordTypeOrderSelected(position: Int) {
        val newOrder = settingsMapper.toCardOrder(position)

        (btnCardOrderManualVisibility as MutableLiveData).value = newOrder == CardOrder.MANUAL

        viewModelScope.launch {
            if (newOrder == CardOrder.MANUAL) {
                openCardOrderDialog(prefsInteractor.getCardOrder())
            }
            prefsInteractor.setCardOrder(newOrder)
            updateCardOrderViewData()
        }
    }

    fun onCardOrderManualClick() {
        openCardOrderDialog(CardOrder.MANUAL)
    }

    fun onFirstDayOfWeekSelected(position: Int) {
        val newDayOfWeek = settingsMapper.toDayOfWeek(position)

        viewModelScope.launch {
            prefsInteractor.setFirstDayOfWeek(newDayOfWeek)
            updateFirstDayOfWeekViewData()
        }
    }

    fun onStartOfDayClicked() {
        viewModelScope.launch {
            DateTimeDialogParams(
                tag = START_OF_DAY_DIALOG_TAG,
                type = DateTimeDialogType.TIME,
                timestamp = prefsInteractor.getStartOfDayShift()
                    .let(settingsMapper::startOfDayShiftToTimeStamp),
                useMilitaryTime = true,
            ).let(router::navigate)
        }
    }

    fun onStartOfDaySignClicked() {
        viewModelScope.launch {
            val newValue = prefsInteractor.getStartOfDayShift() * -1
            prefsInteractor.setStartOfDayShift(newValue)
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            updateStartOfDayViewData()
        }
    }

    fun onShowUntrackedClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowUntrackedInRecords()
            prefsInteractor.setShowUntrackedInRecords(newValue)
            (showUntrackedCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onAllowMultitaskingClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getAllowMultitasking()
            prefsInteractor.setAllowMultitasking(newValue)
            (allowMultitaskingCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onShowNotificationsClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowNotifications()
            prefsInteractor.setShowNotifications(newValue)
            (showNotificationsCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
        }
    }

    fun onInactivityReminderClicked() {
        viewModelScope.launch {
            DurationDialogParams(
                tag = INACTIVITY_DURATION_DIALOG_TAG,
                duration = prefsInteractor.getInactivityReminderDuration()
            ).let(router::navigate)
        }
    }

    fun onDarkModeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getDarkMode()
            prefsInteractor.setDarkMode(newValue)
            (darkModeCheckbox as MutableLiveData).value = newValue
            (themeChanged as MutableLiveData).value = true
        }
    }

    fun onUseMilitaryTimeClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getUseMilitaryTimeFormat()
            prefsInteractor.setUseMilitaryTimeFormat(newValue)
            (useMilitaryTimeCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            updateUseMilitaryTimeViewData()
            updateStartOfDayViewData()
        }
    }

    fun onUseProportionalMinutesClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getUseProportionalMinutes()
            prefsInteractor.setUseProportionalMinutes(newValue)
            (useProportionalMinutesCheckbox as MutableLiveData).value = newValue
            notificationTypeInteractor.updateNotifications()
            widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
            updateUseProportionalMinutesViewData()
        }
    }

    fun onShowRecordTagSelectionClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getShowRecordTagSelection()
            prefsInteractor.setShowRecordTagSelection(newValue)
            (showRecordTagSelectionCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onRecordTagSelectionCloseClicked() {
        viewModelScope.launch {
            val newValue = !prefsInteractor.getRecordTagSelectionCloseAfterOne()
            prefsInteractor.setRecordTagSelectionCloseAfterOne(newValue)
            (recordTagSelectionCloseCheckbox as MutableLiveData).value = newValue
        }
    }

    fun onChangeCardSizeClick() {
        router.navigate(CardSizeDialogParams)
    }

    fun onEditCategoriesClick() {
        router.navigate(CategoriesParams)
    }

    fun onArchiveClick() {
        router.navigate(ArchiveParams)
    }

    fun onDurationSet(tag: String?, duration: Long) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setInactivityReminderDuration(duration)
                updateInactivityReminderViewData()
                // TODO check and schedule inactivity reminder or reschedule at new time?
            }
        }
    }

    fun onDurationDisabled(tag: String?) {
        when (tag) {
            INACTIVITY_DURATION_DIALOG_TAG -> viewModelScope.launch {
                prefsInteractor.setInactivityReminderDuration(0)
                updateInactivityReminderViewData()
                notificationInactivityInteractor.cancel()
            }
        }
    }

    fun onDateTimeSet(timestamp: Long, tag: String?) = viewModelScope.launch {
        when (tag) {
            START_OF_DAY_DIALOG_TAG -> {
                val wasPositive = prefsInteractor.getStartOfDayShift() >= 0
                val newValue = settingsMapper.toStartOfDayShift(timestamp, wasPositive)
                prefsInteractor.setStartOfDayShift(newValue)
                widgetInteractor.updateWidgets(listOf(WidgetType.STATISTICS_CHART))
                updateStartOfDayViewData()
            }
        }
    }

    fun onThemeChanged() {
        (themeChanged as MutableLiveData).value = false
    }

    private fun openCardOrderDialog(cardOrder: CardOrder) {
        router.navigate(
            CardOrderDialogParams(cardOrder)
        )
    }

    private fun showMessage(stringResId: Int) {
        val params = ToastParams(message = resourceRepo.getString(stringResId))
        router.show(params)
    }

    private suspend fun updateCardOrderViewData() {
        val data = loadCardOrderViewData()
        (cardOrderViewData as MutableLiveData).value = data
    }

    private suspend fun loadCardOrderViewData(): CardOrderViewData {
        return prefsInteractor.getCardOrder()
            .let(settingsMapper::toCardOrderViewData)
    }

    private suspend fun updateFirstDayOfWeekViewData() {
        val data = loadFirstDayOfWeekViewData()
        (firstDayOfWeekViewData as MutableLiveData).value = data
    }

    private suspend fun loadFirstDayOfWeekViewData(): FirstDayOfWeekViewData {
        return prefsInteractor.getFirstDayOfWeek()
            .let(settingsMapper::toFirstDayOfWeekViewData)
    }

    private suspend fun updateStartOfDayViewData() {
        val data = loadStartOfDayViewData()
        startOfDayViewData.set(data)
    }

    private suspend fun loadStartOfDayViewData(): SettingsStartOfDayViewData {
        val shift = prefsInteractor.getStartOfDayShift()
        val useMilitaryTimeFormat = prefsInteractor.getUseMilitaryTimeFormat()

        return SettingsStartOfDayViewData(
            startOfDayValue = settingsMapper.toStartOfDayText(
                startOfDayShift = shift,
                useMilitaryTime = useMilitaryTimeFormat,
            ),
            startOfDaySign = settingsMapper.toStartOfDaySign(
                shift = shift
            )
        )
    }

    private suspend fun updateInactivityReminderViewData() {
        val data = loadInactivityReminderViewData()
        (inactivityReminderViewData as MutableLiveData).value = data
    }

    private suspend fun loadInactivityReminderViewData(): String {
        return prefsInteractor.getInactivityReminderDuration()
            .let(settingsMapper::toInactivityReminderText)
    }

    private suspend fun updateUseMilitaryTimeViewData() {
        val data = loadUseMilitaryTimeViewData()
        (useMilitaryTimeHint as MutableLiveData).value = data
    }

    private suspend fun updateUseProportionalMinutesViewData() {
        val data = loadUseProportionalMinutesViewData()
        (useProportionalMinutesHint as MutableLiveData).value = data
    }

    private suspend fun loadUseMilitaryTimeViewData(): String {
        return prefsInteractor.getUseMilitaryTimeFormat()
            .let(settingsMapper::toUseMilitaryTimeHint)
    }

    private suspend fun loadUseProportionalMinutesViewData(): String {
        return prefsInteractor.getUseProportionalMinutes()
            .let(settingsMapper::toUseProportionalMinutesHint)
    }

    companion object {
        private const val INACTIVITY_DURATION_DIALOG_TAG = "inactivity_duration_dialog_tag"
        private const val START_OF_DAY_DIALOG_TAG = "start_of_day_dialog_tag"
    }
}
