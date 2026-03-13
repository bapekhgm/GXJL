package com.example.processrecord.ui.screen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.processrecord.MainActivity
import com.example.processrecord.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(maxSdkVersion = 35)
class HomeAddRecordFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addRecordButton_opensEntryScreen_andBackReturnsOverview() {
        val context = composeRule.activity

        composeRule.onNodeWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.work_record_title_add))
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription(context.getString(R.string.common_back))
            .performClick()

        composeRule.onNodeWithText(context.getString(R.string.home_tab_daily_records))
            .assertIsDisplayed()
    }

    @Test
    fun saveNewRecord_returnsOverviewAndShowsRecord() {
        val suffix = System.currentTimeMillis()
        val processName = "UiAddProc$suffix"
        val styleName = "UiStyle$suffix"

        composeRule.onNodeWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        openAddProcessDialog()
        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG).performTextInput(processName)
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextInput("1.5")
        composeRule.onNodeWithTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.readEditableText(PROCESS_INPUT_TEST_TAG).contains(processName)
        }

        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextInput(styleName)
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextInput("23")

        composeRule.onNodeWithTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(styleName, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText(styleName, substring = true)
            .assertCountEquals(1)
    }

    private fun openAddProcessDialog() {
        composeRule.onNodeWithTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(ADD_PROCESS_MENU_ITEM_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
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
