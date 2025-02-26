package com.example.util.simpletimetracker

import android.widget.DatePicker
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.contrib.PickerActions.setDate
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.util.simpletimetracker.utils.BaseUiTest
import com.example.util.simpletimetracker.utils.NavUtils
import com.example.util.simpletimetracker.utils.checkViewIsDisplayed
import com.example.util.simpletimetracker.utils.clickOnView
import com.example.util.simpletimetracker.utils.clickOnViewWithId
import com.example.util.simpletimetracker.utils.clickOnViewWithText
import com.example.util.simpletimetracker.utils.longClickOnViewWithId
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordsRangesTest : BaseUiTest() {

    @Test
    fun recordsRanges() {
        val name = "Test"

        // Add activity
        NavUtils.addActivity(name)

        // Start timer
        clickOnViewWithText(name)
        clickOnView(allOf(isDescendantOfA(withId(R.id.viewRunningRecordItem)), withText(name)))

        // Records
        NavUtils.openRecordsScreen()
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                isCompletelyDisplayed()
            )
        )
        clickOnViewWithId(R.id.btnRecordsContainerPrevious)
        checkViewIsDisplayed(
            allOf(
                withId(R.id.viewRecordItem),
                hasDescendant(withText(R.string.untracked_time_name)),
                isCompletelyDisplayed()
            )
        )

        longClickOnViewWithId(R.id.btnRecordsContainerToday)
        checkViewIsDisplayed(allOf(withText(name), isCompletelyDisplayed()))

        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
        clickOnViewWithId(R.id.btnRecordsContainerNext)
        checkViewIsDisplayed(allOf(withText(R.string.records_empty), isCompletelyDisplayed()))
    }

    @Test
    fun selectNearDate() {
        NavUtils.openRecordsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        val calendarNext = Calendar.getInstance().apply {
            add(Calendar.DATE, 1)
        }

        // Check yesterday
        clickOnViewWithId(R.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(R.string.title_yesterday), isCompletelyDisplayed()))

        // Check tomorrow
        clickOnViewWithId(R.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(R.string.title_tomorrow), isCompletelyDisplayed()))
    }

    @Test
    fun selectFarDate() {
        NavUtils.openRecordsScreen()

        val calendarPrev = Calendar.getInstance().apply {
            set(Calendar.YEAR, 1950)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titlePrev = dayTitleFormat.format(calendarPrev.timeInMillis)
        val calendarNext = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2050)
            set(Calendar.MONTH, 0)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val titleNext = dayTitleFormat.format(calendarNext.timeInMillis)

        // Check prev date
        clickOnViewWithId(R.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarPrev.get(Calendar.YEAR),
                    calendarPrev.get(Calendar.MONTH) + 1,
                    calendarPrev.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titlePrev), isCompletelyDisplayed()))

        // Check next date
        clickOnViewWithId(R.id.btnRecordsContainerToday)
        onView(withClassName(equalTo(DatePicker::class.java.name)))
            .perform(
                setDate(
                    calendarNext.get(Calendar.YEAR),
                    calendarNext.get(Calendar.MONTH) + 1,
                    calendarNext.get(Calendar.DAY_OF_MONTH)
                )
            )
        clickOnViewWithId(R.id.btnDateTimeDialogPositive)

        checkViewIsDisplayed(allOf(withText(titleNext), isCompletelyDisplayed()))
    }

    companion object {
        private val dayTitleFormat = SimpleDateFormat("E, MMM d", Locale.getDefault())
    }
}
