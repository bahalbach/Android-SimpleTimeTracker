package com.example.util.simpletimetracker.data_local.repo

import android.content.SharedPreferences
import com.example.util.simpletimetracker.data_local.extension.delegate
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepoImpl @Inject constructor(
    private val prefs: SharedPreferences
) : PrefsRepo {

    private val firstDayOfWeekDefault: Int by lazy {
        Calendar.getInstance().firstDayOfWeek
    }

    override var recordTypesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_RECORD_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var categoriesFilteredOnChart: Set<String> by prefs.delegate(
        KEY_CATEGORIES_TYPES_FILTERED_ON_CHART, emptySet()
    )

    override var chartFilterType: Int by prefs.delegate(
        KEY_CHART_FILTER_TYPE, 0
    )

    override var cardOrder: Int by prefs.delegate(
        KEY_CARD_ORDER, 0
    )

    override var firstDayOfWeek: Int by prefs.delegate(
        KEY_FIRST_DAY_OF_WEEK, firstDayOfWeekDefault
    )

    override var startOfDayShift: Long by prefs.delegate(
        KEY_START_OF_DAY_SHIFT, 0
    )

    override var showUntrackedInRecords: Boolean by prefs.delegate(
        KEY_SHOW_UNTRACKED_IN_RECORDS, true
    )

    override var allowMultitasking: Boolean by prefs.delegate(
        KEY_ALLOW_MULTITASKING, true
    )

    override var showNotifications: Boolean by prefs.delegate(
        KEY_SHOW_NOTIFICATIONS, false
    )

    override var inactivityReminderDuration: Long by prefs.delegate(
        KEY_INACTIVITY_REMINDER_DURATION, 0 // 0 is for disabled
    )

    override var darkMode: Boolean by prefs.delegate(
        KEY_DARK_MODE, false
    )

    override var numberOfCards: Int by prefs.delegate(
        KEY_NUMBER_OF_CARDS, 0
    )

    override var useMilitaryTimeFormat: Boolean by prefs.delegate(
        KEY_USE_MILITARY_TIME_FORMAT, true
    )
    override var useProportionalMinutes: Boolean by prefs.delegate(
        KEY_USE_PROPORTIONAL_MINUTES, false
    )

    override var showRecordTagSelection: Boolean by prefs.delegate(
        KEY_SHOW_RECORD_TAG_SELECTION, false
    )

    override var recordTagSelectionCloseAfterOne: Boolean by prefs.delegate(
        KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE, false
    )

    override fun setWidget(widgetId: Int, recordType: Long) {
        prefs.edit().putLong(KEY_WIDGET + widgetId, recordType).apply()
    }

    override fun getWidget(widgetId: Int): Long {
        return prefs.getLong(KEY_WIDGET + widgetId, 0)
    }

    override fun removeWidget(widgetId: Int) {
        prefs.edit().remove(KEY_WIDGET + widgetId).apply()
    }

    override fun setCardOrderManual(cardOrder: Map<Long, Long>) {
        val set = cardOrder.map { (typeId, order) ->
            "$typeId$CARDS_ORDER_DELIMITER${order.toShort()}"
        }.toSet()

        prefs.edit().putStringSet(KEY_CARD_ORDER_MANUAL, set).apply()
    }

    override fun getCardOrderManual(): Map<Long, Long> {
        val set = prefs.getStringSet(KEY_CARD_ORDER_MANUAL, emptySet())

        return set
            ?.map { string ->
                string.split(CARDS_ORDER_DELIMITER).let { parts ->
                    parts.getOrNull(0).orEmpty() to parts.getOrNull(1).orEmpty()
                }
            }
            ?.toMap()
            ?.mapKeys { it.key.toLongOrNull().orZero() }
            ?.mapValues { it.value.toLongOrNull().orZero() }
            ?: emptyMap()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val CARDS_ORDER_DELIMITER = "_"

        private const val KEY_RECORD_TYPES_FILTERED_ON_CHART = "recordTypesFilteredOnChart"
        private const val KEY_CATEGORIES_TYPES_FILTERED_ON_CHART = "categoriesFilteredOnChart"
        private const val KEY_CHART_FILTER_TYPE = "chartFilterType"
        private const val KEY_CARD_ORDER = "cardOrder"
        private const val KEY_FIRST_DAY_OF_WEEK = "firstDayOfWeek"
        private const val KEY_START_OF_DAY_SHIFT = "startOfDayShift"
        private const val KEY_SHOW_UNTRACKED_IN_RECORDS = "showUntrackedInRecords"
        private const val KEY_ALLOW_MULTITASKING = "allowMultitasking"
        private const val KEY_SHOW_NOTIFICATIONS = "showNotifications"
        private const val KEY_INACTIVITY_REMINDER_DURATION = "inactivityReminderDuration"
        private const val KEY_DARK_MODE = "darkMode"
        private const val KEY_NUMBER_OF_CARDS = "numberOfCards" // 0 - default width
        private const val KEY_USE_MILITARY_TIME_FORMAT = "useMilitaryTimeFormat"
        private const val KEY_USE_PROPORTIONAL_MINUTES = "useProportionalMinutes"
        private const val KEY_SHOW_RECORD_TAG_SELECTION = "showRecordTagSelection"
        private const val KEY_RECORD_TAG_SELECTION_CLOSE_AFTER_ONE = "recordTagSelectionCloseAfterOne"
        private const val KEY_WIDGET = "widget_"
        private const val KEY_CARD_ORDER_MANUAL = "cardOrderManual"

        // Removed
        @Suppress("unused")
        private const val KEY_SORT_RECORD_TYPES_BY_COLOR = "sortRecordTypesByColor" // Boolean
    }
}