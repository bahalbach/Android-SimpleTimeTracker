package com.example.util.simpletimetracker

import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyLeftOf
import androidx.test.espresso.assertion.PositionAssertions.isCompletelyRightOf
import androidx.test.espresso.assertion.PositionAssertions.isLeftAlignedWith
import androidx.test.espresso.assertion.PositionAssertions.isTopAlignedWith
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.contrib.PickerActions.setTime
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.core.extension.setToStartOfDay
import com.example.util.simpletimetracker.core.extension.setWeekToFirstDay
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.Direction
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewDoesNotExist
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.checkViewIsNotDisplayed
import com.example.util.simpletimetracker.utils.clickOnSpinnerWithId
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.drag
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import com.example.util.simpletimetracker.utils.nestedScrollTo
import com.example.util.simpletimetracker.utils.scrollRecyclerToView
import com.example.util.simpletimetracker.utils.tryAction
import com.example.util.simpletimetracker.utils.unconstrainedClickOnView
import com.example.util.simpletimetracker.utils.withPluralText
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsTest : BaseUiTest() {

    @Test
    fun showUntrackedSetting() {
        val name = "Test"
        val color = firstColor
        val icon = firstIcon

        // Add activity
        testUtils.addActivity(name = name, color = color, icon = icon)

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))

        // Untracked is not shown
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )

        // Add record
        NavUtils.addRecord(name)
        checkViewDoesNotExist(
            allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed())
        )
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowUntracked)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowUntracked))
        onView(withId(R.id.checkboxSettingsShowUntracked)).check(matches(isChecked()))

        // Untracked is shown
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(R.string.untracked_time_name), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))
    }

    @Test
    fun allowMultitaskingSetting() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        // Start timers
        tryAction { clickOnViewWithText(name2) }
        clickOnViewWithText(name3)
        var startTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(startTime))
            )
        )

        // Click on already running
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
        NavUtils.openRecordsScreen()
        checkViewDoesNotExist(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewDoesNotExist(allOf(withText(name3), isCompletelyDisplayed()))

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))

        // Click on one not running
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1))
        )
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name1)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name2)))
        )
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name3)))
        )

        // Records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))

        // Click another
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2))
        )
        startTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewDoesNotExist(
            allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1)))
        )

        // Record added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))

        // Change setting back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsAllowMultitasking))
        onView(withId(R.id.checkboxSettingsAllowMultitasking)).check(matches(isChecked()))

        // Start another timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(
            allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name3))
        )
        val newStartTime = System.currentTimeMillis()
            .let { timeMapper.formatTime(it, true) }
        tryAction {
            checkViewIsDisplayed(
                allOf(
                    withId(R.id.viewRunningRecordItem),
                    hasDescendant(withText(name2)),
                    hasDescendant(withText(startTime))
                )
            )
        }
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRunningRecordItem),
                hasDescendant(withText(name3)),
                hasDescendant(withText(newStartTime))
            )
        )

        // No new records added
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name1), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name2), isCompletelyDisplayed()))
        checkViewIsDisplayed(allOf(withText(name3), isCompletelyDisplayed()))
    }

    @Test
    fun cardSizeTest() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)
        testUtils.addActivity(name3)

        tryAction { check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) } }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Open settings
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)

        // Check order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Change setting
        clickOnViewWithText("6")
        clickOnViewWithText("5")
        clickOnViewWithText("4")
        clickOnViewWithText("3")
        clickOnViewWithText("2")
        clickOnViewWithText("1")

        // Check new order
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }

        // Change back
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)
        check(name1, name2) { matcher -> isCompletelyAbove(matcher) }
        check(name2, name3) { matcher -> isCompletelyAbove(matcher) }
        clickOnViewWithText(R.string.card_size_default)

        // Check order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
    }

    @Test
    fun cardOrderByName() {
        val name1 = "Test1"
        val name2 = "Test2"
        val color1 = firstColor
        val color2 = lastColor

        // Add activities
        testUtils.addActivity(name = name1, color = color2)
        testUtils.addActivity(name = name2, color = color1)

        // Check order
        tryAction { check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) } }

        // Check settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.spinnerSettingsRecordTypeSort)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_by_name))
        )
    }

    @Test
    fun cardOrderByColor() {
        val name = "Test"

        val colors = ColorMapper.getAvailableColors()
        val black = colors.first()
        val blueGrey = colors.last()

        // Restore color color by moving some colors.
        val colorMap = colors.drop(1).dropLast(1)
            .map {
                it to false
            }
            .toMutableList()
            .apply {
                add(2, 0xffff00fc.toInt() to true) // custom color hue 300
                add(7, blueGrey to false)
                add(11, 0xff34664d.toInt() to true) // custom color hsv 150, 49, 40
                add(12, 0xff418061.toInt() to true) // custom color hsv 150, 49, 50
                add(13, 0xff4e9974.toInt() to true) // custom color hsv 150, 49, 60
                add(14, 0xff80ffc0.toInt() to true) // custom color hsv 150, 49, 100
                add(15, 0xff00ff81.toInt() to true) // custom color hsv 150, 100, 100
                add(21, 0xffffae00.toInt() to true) // custom color hue 40
                add(black to false)
            }.mapIndexed { index, color ->
                index to color
            }

        // Add activities
        colorMap.shuffled().forEach { (index, color) ->
            val colorId = color.first.takeUnless { color.second }
            val colorInt = color.first.takeIf { color.second }
            testUtils.addActivity(name = name + index, color = colorId, colorInt = colorInt)
        }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        clickOnViewWithText("1")
        pressBack()

        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_by_color)
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_by_color))
        )

        // Check new order
        NavUtils.openRunningRecordsScreen()

        colorMap.forEach { (index, _) ->
            if (index == 0) return@forEach

            val currentItem = name + index
            val previousItem = name + (index - 1)

            scrollRecyclerToView(R.id.rvRunningRecordsList, hasDescendant(withText(currentItem)))
            tryAction { check(previousItem, currentItem) { matcher -> isCompletelyAbove(matcher) } }
        }
    }

    @Test
    fun cardOrderManual() {
        val name1 = "Test1"
        val name2 = "Test2"
        val name3 = "Test3"

        // Add activities
        testUtils.addActivity(name3)
        testUtils.addActivity(name2)
        testUtils.addActivity(name1)

        // Change settings
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)

        // Check old order
        check(name1, name2) { matcher -> isCompletelyLeftOf(matcher) }
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Drag
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name2)))
            .perform(drag(Direction.LEFT, 300))

        // Check new order
        pressBack()
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsRecordTypeSortValue), withText(R.string.settings_sort_manually))
        )
        NavUtils.openRunningRecordsScreen()
        check(name2, name1) { matcher -> isCompletelyLeftOf(matcher) }
        check(name1, name3) { matcher -> isCompletelyLeftOf(matcher) }

        // Change order
        NavUtils.openSettingsScreen()
        onView(withId(R.id.btnCardOrderManual)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.btnCardOrderManual)
        check(name2, name1) { matcher -> isCompletelyLeftOf(matcher) }
        check(name1, name3) { matcher -> isCompletelyLeftOf(matcher) }
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(name1)))
            .perform(drag(Direction.RIGHT, 300))

        // Check new order
        pressBack()
        NavUtils.openRunningRecordsScreen()
        check(name2, name3) { matcher -> isCompletelyLeftOf(matcher) }
        check(name3, name1) { matcher -> isCompletelyLeftOf(matcher) }
    }

    @Test
    fun cardOrderManual2() {
        val name = "Test"

        // Add activities
        (1..15).forEach {
            testUtils.addActivity("$name$it")
        }

        // Change settings
        NavUtils.openSettingsScreen()
        NavUtils.openCardSizeScreen()
        Thread.sleep(1000)
        clickOnViewWithText("4")
        pressBack()
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_by_color)
        clickOnSpinnerWithId(R.id.spinnerSettingsRecordTypeSort)
        clickOnViewWithText(R.string.settings_sort_manually)
        Thread.sleep(1000)

        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        // Drag
        (1..15).forEach {
            onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText("$name$it")))
                .perform(
                    drag(Direction.RIGHT, screenWidth),
                    drag(Direction.DOWN, screenHeight),
                )
        }

        // Check order in settings
        checkManualOrder(name)

        // Check order on main
        pressBack()
        NavUtils.openRunningRecordsScreen()
        checkManualOrder(name)
    }

    @Test
    fun enableNotifications() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Start one timer
        tryAction { clickOnViewWithText(name1) }

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))

        // Stop first timer
        NavUtils.openRunningRecordsScreen()
        clickOnView(allOf(withId(R.id.viewRunningRecordItem), hasDescendant(withText(name1))))

        // Start another timer
        clickOnViewWithText(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowNotifications)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowNotifications))
        onView(withId(R.id.checkboxSettingsShowNotifications)).check(matches(isNotChecked()))
    }

    @Test
    fun enableEnableDarkMode() {
        val name1 = "Test1"
        val name2 = "Test2"

        // Add activities
        testUtils.addActivity(name1)
        testUtils.addActivity(name2)

        // Start one timer
        tryAction { clickOnViewWithText(name1) }

        // Add record
        testUtils.addRecord(name1)
        testUtils.addRecord(name2)

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()

        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsDarkMode)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsDarkMode))
        onView(withId(R.id.checkboxSettingsDarkMode)).check(matches(isNotChecked()))

        // Check screens
        NavUtils.openRunningRecordsScreen()
        NavUtils.openRecordsScreen()
        NavUtils.openStatisticsScreen()
        NavUtils.openSettingsScreen()
    }

    @Test
    fun inactivityReminder() {
        // Change settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsInactivityReminder)).perform(nestedScrollTo())
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsInactivityReminderTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )

        // 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$secondString"))

        // 1m
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString"))

        // 1h
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString"))

        // 1m 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.ivDurationPickerDelete)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$minuteString 01$secondString"))

        // 1h 1m 1s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard1)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 01$minuteString 01$secondString"))

        // 1h 30m
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clearDuration()
        clickOnViewWithId(R.id.tvNumberKeyboard9)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithId(R.id.tvNumberKeyboard0)
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("1$hourString 30$minuteString"))

        // 99h 99m 99s
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        repeat(10) { clickOnViewWithId(R.id.ivDurationPickerDelete) }
        repeat(10) { clickOnViewWithId(R.id.tvNumberKeyboard9) }
        clickOnViewWithText(R.string.duration_dialog_save)
        checkViewIsDisplayed(withText("100$hourString 40$minuteString 39$secondString"))

        // Disable
        clickOnViewWithId(R.id.groupSettingsInactivityReminder)
        clickOnViewWithText(R.string.duration_dialog_disable)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.tvSettingsInactivityReminderTime),
                withText(R.string.settings_inactivity_reminder_disabled)
            )
        )
    }

    @Test
    fun militaryTime() {
        // Check settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.tvSettingsUseMilitaryTimeHint)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseMilitaryTime)
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isNotChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withSubstring("1:00")))

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseMilitaryTime)
        onView(withId(R.id.checkboxSettingsUseMilitaryTime)).check(matches(isChecked()))
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsUseMilitaryTimeHint), withText("13:00")))
    }

    @Test
    fun proportionalMinutes() {
        val name = "Test"

        fun checkView(id: Int, text: String) {
            checkViewIsDisplayed(allOf(withId(id), hasDescendant(withText(text)), isCompletelyDisplayed()))
        }

        fun checkFormat(timeString: String) {
            NavUtils.openRecordsScreen()
            checkView(R.id.viewRecordItem, timeString)
            NavUtils.openStatisticsScreen()
            checkView(R.id.viewStatisticsItem, timeString)
            tryAction { clickOnView(allOf(withText(name), isCompletelyDisplayed())) }
            checkView(R.id.cardStatisticsDetailTotal, timeString)
            pressBack()
        }

        // Add data
        val timeEnded = System.currentTimeMillis()
        val timeStarted = timeEnded - TimeUnit.MINUTES.toMillis(75)
        val timeFormat1 = "1$hourString 15$minuteString"
        val timeFormat2 = "%.2f$hourString".format(1.25)
        testUtils.addActivity(name)
        testUtils.addRecord(name, timeStarted, timeEnded)

        // Check format
        checkFormat(timeFormat1)

        // Check settings
        NavUtils.openSettingsScreen()
        onView(withId(R.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Change settings
        clickOnViewWithId(R.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withSubstring(timeFormat2))
        )

        // Check format after setting change
        checkFormat(timeFormat2)

        // Change settings back
        NavUtils.openSettingsScreen()
        onView(withId(R.id.tvSettingsUseProportionalMinutesHint)).perform(nestedScrollTo())
        clickOnViewWithId(R.id.checkboxSettingsUseProportionalMinutes)
        onView(withId(R.id.checkboxSettingsUseProportionalMinutes)).check(matches(isNotChecked()))
        checkViewIsDisplayed(
            allOf(withId(R.id.tvSettingsUseProportionalMinutesHint), withText(timeFormat1))
        )

        // Check format again
        checkFormat(timeFormat1)
    }

    @Test
    fun firstDayOfWeek() {
        // If today is sunday:
        // add record for previous monday,
        // then select first day monday - record will be present this week,
        // then select first day sunday - record will be prev week.
        // If today is not sunday:
        // add record for prev sunday,
        // then select first day sunday - record will be present this week,
        // then select first day monday - record will be prev week.

        val name = "Test"
        val isTodaySunday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY

        // Add data
        testUtils.addActivity(name)
        val calendar = Calendar.getInstance()
            .apply {
                val recordDay = if (isTodaySunday) Calendar.MONDAY else Calendar.SUNDAY
                firstDayOfWeek = recordDay
                setWeekToFirstDay()
                set(Calendar.HOUR_OF_DAY, 15)
            }
        testUtils.addRecord(
            typeName = name,
            timeStarted = calendar.timeInMillis,
            timeEnded = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        )

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(R.string.day_of_week_monday)
        } else {
            clickOnViewWithText(R.string.day_of_week_sunday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithText(R.string.range_week)
        clickOnView(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                ViewMatchers.hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        var titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
        )
        longClickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)

        // Change setting
        NavUtils.openSettingsScreen()
        clickOnSpinnerWithId(R.id.spinnerSettingsFirstDayOfWeek)
        if (isTodaySunday) {
            clickOnViewWithText(R.string.day_of_week_sunday)
        } else {
            clickOnViewWithText(R.string.day_of_week_monday)
        }

        // Check statistics
        NavUtils.openStatisticsScreen()
        checkViewDoesNotExist(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnView(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(withText(name)),
                isCompletelyDisplayed()
            )
        )

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithText(R.string.range_week)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 0),
                ViewMatchers.hasSibling(withText("0")),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, 1),
                ViewMatchers.hasSibling(withText("1")),
                isCompletelyDisplayed()
            )
        )

        // Check range titles
        titlePrev = timeMapper.toWeekTitle(
            weeksFromToday = -1,
            firstDayOfWeek = if (isTodaySunday) DayOfWeek.SUNDAY else DayOfWeek.MONDAY
        )
        longClickOnViewWithId(R.id.btnStatisticsDetailToday)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        pressBack()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
    }

    @Test
    fun startOfDay() {
        fun Long.toTimePreview() = timeMapper.formatTime(this, true)

        val name = "Test"

        // Add data
        testUtils.addActivity(name)
        val calendar = Calendar.getInstance().apply {
            setToStartOfDay()
            add(Calendar.DATE, -2)
        }
        var startOfDayTimeStamp = calendar.timeInMillis
        val timeStartedTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(22)
        val timeEndedTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(26)
        var startOfDayPreview = startOfDayTimeStamp.toTimePreview()
        val timeStartedPreview = timeStartedTimeStamp.toTimePreview()
        val timeEndedPreview = timeEndedTimeStamp.toTimePreview()
        testUtils.addRecord(
            typeName = name,
            timeStarted = timeStartedTimeStamp,
            timeEnded = timeEndedTimeStamp
        )

        // Check records
        NavUtils.openRecordsScreen()
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 22)
        checkStatisticsItem(name = name, hours = 2)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Check setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.tvSettingsStartOfDayTime)).perform(nestedScrollTo())
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(R.id.btnSettingsStartOfDaySign))

        // Change setting to +1
        clickOnView(withId(R.id.groupSettingsStartOfDay))
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(1, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check new setting
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.plus_sign)))
        )

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to -1
        NavUtils.openSettingsScreen()
        onView(withId(R.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())

        // Check new setting
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.minus_sign)))
        )

        startOfDayTimeStamp = calendar.timeInMillis - TimeUnit.HOURS.toMillis(1)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = startOfDayPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = startOfDayPreview, timeEnd = timeEndedPreview)

        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 23)
        checkStatisticsItem(name = name, hours = 1)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 21)
        checkStatisticsItem(name = name, hours = 3)

        // Check detailed statistics
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to +2, record will be shifted out from one day
        startOfDayTimeStamp = calendar.timeInMillis + TimeUnit.HOURS.toMillis(2)
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(2, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsDisplayed(
            allOf(withId(R.id.btnSettingsStartOfDaySign), hasDescendant(withText(R.string.minus_sign)))
        )
        onView(withId(R.id.btnSettingsStartOfDaySign)).perform(nestedScrollTo(), click())
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))

        // Check records
        NavUtils.openRecordsScreen()
        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(name = name, timeStart = timeStartedPreview, timeEnd = timeEndedPreview)
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkRecord(
            nameResId = R.string.untracked_time_name, timeStart = startOfDayPreview, timeEnd = startOfDayPreview,
        )
        // Check statistics
        NavUtils.openStatisticsScreen()
        longClickOnViewWithId(R.id.btnStatisticsContainerToday)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 20)
        checkStatisticsItem(name = name, hours = 4)
        clickOnViewWithId(R.id.btnStatisticsContainerNext)
        checkStatisticsItem(nameResId = R.string.untracked_time_name, hours = 24)

        // Check detailed statistics
        clickOnViewWithId(R.id.btnStatisticsContainerPrevious)
        clickOnView(allOf(withText(name), isCompletelyDisplayed()))
        clickOnView(allOf(withText(R.string.range_overall), isCompletelyDisplayed()))
        clickOnViewWithText(R.string.range_day)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(1)
        clickOnViewWithId(R.id.btnStatisticsDetailPrevious)
        checkStatisticsDetailRecords(0)
        pressBack()

        // Change setting to 0
        startOfDayTimeStamp = calendar.timeInMillis
        startOfDayPreview = startOfDayTimeStamp.toTimePreview()

        NavUtils.openSettingsScreen()
        onView(withId(R.id.groupSettingsStartOfDay)).perform(nestedScrollTo(), click())
        onView(withClassName(equalTo(TimePicker::class.java.name))).perform(setTime(0, 0))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)
        checkViewIsDisplayed(allOf(withId(R.id.tvSettingsStartOfDayTime), withText(startOfDayPreview)))
        checkViewIsNotDisplayed(withId(R.id.btnSettingsStartOfDaySign))
    }

    @Test
    fun showRecordTagSelection() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"

        // Add data
        testUtils.addActivity(name)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Has a tag - show dialog
        clickOnViewWithText(name)
        checkViewIsDisplayed(withText(R.string.change_record_untagged))
        checkViewIsDisplayed(withText(tag))
        pressBack()
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Start untagged
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        clickOnView(withText(R.string.change_record_untagged))
        pressBack()
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Start tagged
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        pressBack()
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).check(matches(isNotChecked()))

        // Start with tags - no dialog
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }
    }

    @Test
    fun recordTagSelectionClose() {
        val name = "TypeName"
        val tag = "TagName"
        val tagGeneral = "TagGeneral"
        val fullName = "$name - $tag"
        val fullName2 = "$name - $tag, $tagGeneral"

        // Add data
        testUtils.addActivity(name)
        tryAction { clickOnViewWithText(name) }
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsNotDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))

        // No tags - started right away
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name))) }

        // Add tag
        testUtils.addRecordTag(tag, name)
        testUtils.addRecordTag(tagGeneral)

        // Start after one tag selected
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).perform(nestedScrollTo())
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isChecked()))
        unconstrainedClickOnView(withId(R.id.checkboxSettingsRecordTagSelectionClose))
        onView(withId(R.id.checkboxSettingsRecordTagSelectionClose)).check(matches(isNotChecked()))

        // Start with several tags
        NavUtils.openRunningRecordsScreen()
        clickOnViewWithText(name)
        clickOnView(withText(tag))
        clickOnView(withText(tagGeneral))
        pressBack()
        tryAction { clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(fullName2))) }

        // Change setting
        NavUtils.openSettingsScreen()
        onView(withId(R.id.checkboxSettingsShowRecordTagSelection)).perform(nestedScrollTo())
        checkViewIsDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))

        unconstrainedClickOnView(withId(R.id.checkboxSettingsShowRecordTagSelection))
        checkViewIsNotDisplayed(withText(R.string.settings_show_record_tag_close_hint))
        checkViewIsNotDisplayed(withId(R.id.checkboxSettingsRecordTagSelectionClose))
    }

    @Test
    fun csvExportSettings() {
        NavUtils.openSettingsScreen()
        onView(withId(R.id.layoutSettingsExportCsv)).perform(nestedScrollTo(), click())

        // View is set up
        val currentTime = System.currentTimeMillis()
        var timeStarted = timeMapper.formatDateTime(currentTime - TimeUnit.DAYS.toMillis(7), true)
        var timeEnded = timeMapper.formatDateTime(currentTime, true)
        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))
        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val hourStarted = 15
        val minutesStarted = 16
        val hourEnded = 17
        val minutesEnded = 19
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Set time started
        clickOnViewWithId(R.id.tvCsvExportSettingsTimeStarted)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(allOf(isDescendantOfA(withId(R.id.tabsDateTimeDialog)), withText(R.string.date_time_dialog_time)))
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourStarted, minutesStarted))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Check time set
        val timeStartedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourStarted)
            set(Calendar.MINUTE, minutesStarted)
            timeInMillis
        }
        timeStarted = timeStartedTimestamp
            .let { timeMapper.formatDateTime(it, true) }

        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeStarted), withText(timeStarted)))

        // Set time ended
        clickOnViewWithId(R.id.tvCsvExportSettingsTimeEnded)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(setDate(year, month + 1, day))
        clickOnView(allOf(isDescendantOfA(withId(R.id.tabsDateTimeDialog)), withText(R.string.date_time_dialog_time)))
        onView(withClassName(equalTo(TimePicker::class.java.name)))
            .perform(setTime(hourEnded, minutesEnded))
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        // Check time set
        val timeEndedTimestamp = Calendar.getInstance().run {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hourEnded)
            set(Calendar.MINUTE, minutesEnded)
            timeInMillis
        }
        timeEnded = timeEndedTimestamp
            .let { timeMapper.formatDateTime(it, true) }

        checkViewIsDisplayed(allOf(withId(R.id.tvCsvExportSettingsTimeEnded), withText(timeEnded)))
    }

    private fun clearDuration() {
        repeat(6) { clickOnViewWithId(R.id.ivDurationPickerDelete) }
    }

    private fun check(first: String, second: String, matcher: (Matcher<View>) -> ViewAssertion) {
        onView(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(first))).check(
            matcher(allOf(isDescendantOfA(withId(R.id.viewRecordTypeItem)), withText(second)))
        )
    }

    private fun checkRecord(
        name: String = "",
        nameResId: Int? = null,
        timeStart: String,
        timeEnd: String,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(allOf(withId(R.id.tvRecordItemTimeStarted), withText(timeStart))),
                hasDescendant(allOf(withId(R.id.tvRecordItemTimeFinished), withText(timeEnd))),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkStatisticsItem(
        name: String = "",
        nameResId: Int? = null,
        hours: Int,
    ) {
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewStatisticsItem),
                hasDescendant(if (nameResId != null) withText(nameResId) else withText(name)),
                hasDescendant(withSubstring("$hours$hourString 0$minuteString")),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkStatisticsDetailRecords(count: Int) {
        checkViewIsDisplayed(
            allOf(
                withPluralText(R.plurals.statistics_detail_times_tracked, count),
                ViewMatchers.hasSibling(withText(count.toString())),
                isCompletelyDisplayed()
            )
        )
    }

    private fun checkManualOrder(name: String) {
        check(name + 2, name + 1) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 3, name + 2) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 4, name + 3) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 5, name + 1) { matcher ->
            isCompletelyBelow(matcher)
            isLeftAlignedWith(matcher)
        }
        check(name + 6, name + 5) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 7, name + 6) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 8, name + 7) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 9, name + 5) { matcher ->
            isCompletelyBelow(matcher)
            isLeftAlignedWith(matcher)
        }
        check(name + 10, name + 9) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 11, name + 10) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 12, name + 11) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }

        check(name + 13, name + 9) { matcher ->
            isCompletelyBelow(matcher)
        }
        check(name + 14, name + 13) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
        check(name + 15, name + 14) { matcher ->
            isCompletelyRightOf(matcher)
            isTopAlignedWith(matcher)
        }
    }
}
