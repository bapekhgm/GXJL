package com.example.processrecord.ui.screen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.processrecord.MainActivity
import com.example.processrecord.R
import com.example.processrecord.ui.component.RECORD_CALENDAR_DAY_TEST_TAG_PREFIX
import com.example.processrecord.ui.component.RECORD_CALENDAR_NEXT_MONTH_TEST_TAG
import com.example.processrecord.ui.component.RECORD_CALENDAR_PREVIOUS_MONTH_TEST_TAG
import java.util.Calendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeMonthlyStatsSyncTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun monthlyOverview_followsSelectedMonth_and_monthlyStats_showAllMonths() {
        val suffix = System.currentTimeMillis()
        val processName = "MonthProc$suffix"
        val currentStyle = "Current$suffix"
        val previousStyle = "Previous$suffix"
        val unitPrice = "2.50"
        val currentQuantity = "4"
        val previousQuantity = "7"
        val previousAmount = "17.50"

        val currentMonthDayOneTag = calendarDayTag(monthOffset = 0, day = 1)
        val previousMonthDayOneTag = calendarDayTag(monthOffset = -1, day = 1)

        composeRule.onNodeWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        openAddProcessDialog()
        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG).performTextInput(processName)
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextInput(unitPrice)
        composeRule.onNodeWithTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.readEditableText(PROCESS_INPUT_TEST_TAG).contains(processName)
        }

        selectRecordDate(previousMonthDayOneTag, monthDelta = -1)
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextInput(previousStyle)
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextInput(previousQuantity)
        closeSoftKeyboard()
        composeRule.onNodeWithTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
                .fetchSemanticsNodes().isNotEmpty()
        }

        selectOverviewDate(previousMonthDayOneTag, monthDelta = -1)
        composeRule.onNodeWithTag(HOME_MONTH_OVERVIEW_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(HOME_MONTH_OVERVIEW_CARD_TEST_TAG)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("¥$previousAmount").assertIsDisplayed()
        composeRule.onAllNodesWithText(previousStyle).assertCountEquals(0)

        composeRule.onNodeWithTag(HOME_MONTH_STATS_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(previousStyle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithTag(HOME_MONTH_OVERVIEW_CARD_TEST_TAG).assertCountEquals(0)

        composeRule.onAllNodesWithText(currentStyle).assertCountEquals(0)

        composeRule.onNodeWithTag(HOME_DAILY_RECORDS_TAB_TEST_TAG).performClick()
        composeRule.onNodeWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        selectExistingProcess(processName)
        selectRecordDate(currentMonthDayOneTag)
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextInput(currentStyle)
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextInput(currentQuantity)
        closeSoftKeyboard()
        composeRule.onNodeWithTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
                .fetchSemanticsNodes().isNotEmpty()
        }

        selectOverviewDate(currentMonthDayOneTag, monthDelta = 1)
        composeRule.onNodeWithTag(HOME_MONTH_OVERVIEW_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag(HOME_MONTH_OVERVIEW_CARD_TEST_TAG)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("¥$previousAmount").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(HOME_MONTH_OVERVIEW_CARD_TEST_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithText(previousStyle).assertCountEquals(0)

        composeRule.onNodeWithTag(HOME_MONTH_STATS_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(currentStyle).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(previousStyle).performScrollTo()
        composeRule.onNodeWithText(previousStyle).assertIsDisplayed()

        composeRule.onNodeWithTag(HOME_DAILY_RECORDS_TAB_TEST_TAG).performClick()
        selectOverviewDate(previousMonthDayOneTag, monthDelta = -1)

        composeRule.onNodeWithTag(HOME_MONTH_OVERVIEW_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("¥$previousAmount").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("¥$previousAmount").assertIsDisplayed()

        composeRule.onNodeWithTag(HOME_MONTH_STATS_TAB_TEST_TAG).performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(previousStyle).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(currentStyle).performScrollTo()
        composeRule.onNodeWithText(currentStyle).assertIsDisplayed()
    }

    private fun openAddProcessDialog() {
        composeRule.onNodeWithTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(ADD_PROCESS_MENU_ITEM_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
    }

    private fun selectExistingProcess(processName: String) {
        composeRule.onNodeWithTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText(processName)
            .assertIsDisplayed()
            .performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.readEditableText(PROCESS_INPUT_TEST_TAG).contains(processName)
        }
    }

    private fun selectRecordDate(targetDateTag: String, monthDelta: Int = 0) {
        composeRule.onNodeWithTag(RECORD_DATE_FIELD_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        navigateCalendarMonth(monthDelta)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(targetDateTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(targetDateTag).performClick()
        waitForCalendarToClose()
    }

    private fun selectOverviewDate(targetDateTag: String, monthDelta: Int = 0) {
        composeRule.onNodeWithTag(HOME_SELECTED_DATE_CHIP_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        navigateCalendarMonth(monthDelta)
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(targetDateTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(targetDateTag).performClick()
        waitForCalendarToClose()
    }

    private fun navigateCalendarMonth(monthDelta: Int) {
        repeat(kotlin.math.abs(monthDelta)) {
            composeRule.onNodeWithTag(
                if (monthDelta < 0) {
                    RECORD_CALENDAR_PREVIOUS_MONTH_TEST_TAG
                } else {
                    RECORD_CALENDAR_NEXT_MONTH_TEST_TAG
                }
            ).performClick()
        }
    }

    private fun waitForCalendarToClose() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(RECORD_CALENDAR_PREVIOUS_MONTH_TEST_TAG)
                .fetchSemanticsNodes().isEmpty()
        }
    }

    private fun closeSoftKeyboard() {
        runCatching { Espresso.closeSoftKeyboard() }
        composeRule.waitForIdle()
    }

    private fun calendarDayTag(monthOffset: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, monthOffset)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$RECORD_CALENDAR_DAY_TEST_TAG_PREFIX%04d-%02d-%02d".format(year, month, day)
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.readEditableText(
        tag: String
    ): String {
        val node = onNodeWithTag(tag).fetchSemanticsNode()
        return if (node.config.contains(SemanticsProperties.EditableText)) {
            node.config[SemanticsProperties.EditableText].text
        } else if (node.config.contains(SemanticsProperties.Text)) {
            node.config[SemanticsProperties.Text].joinToString(separator = "") { value ->
                value.text
            }
        } else {
            ""
        }
    }
}
