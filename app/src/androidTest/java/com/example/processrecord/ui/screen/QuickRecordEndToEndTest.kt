package com.example.processrecord.ui.screen

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.processrecord.MainActivity
import com.example.processrecord.R
import java.math.BigDecimal
import java.math.RoundingMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecordEntryEndToEndTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun realEntryFlow_savesTwoRecordsAndShowsOverviewAndMonthlyStats() {
        val suffix = System.currentTimeMillis()
        val processName = "E2EProc$suffix"
        val styleName = "E2EStyle$suffix"
        val unitPrice = "2.${((suffix % 89) + 10).toString().padStart(2, '0')}"
        val firstQuantity = ((suffix % 23) + 11).toString()
        val secondQuantity = (((suffix / 10) % 17) + 5).toString()
        val firstAmount = calculateAmount(unitPrice, firstQuantity)
        val secondAmount = calculateAmount(unitPrice, secondQuantity)
        val totalAmount = calculateAmount(
            unitPrice,
            (firstQuantity.toInt() + secondQuantity.toInt()).toString()
        )

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

        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextInput(styleName)
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextInput(firstQuantity)
        composeRule.onNodeWithTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(styleName, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag(HOME_ADD_RECORD_BUTTON_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        selectExistingProcess(processName)
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(STYLE_INPUT_TEST_TAG).performTextInput(styleName)
        composeRule.onNodeWithTag(UNIT_PRICE_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(UNIT_PRICE_INPUT_TEST_TAG).performTextInput(unitPrice)
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextClearance()
        composeRule.onNodeWithTag(QUANTITY_INPUT_TEST_TAG).performTextInput(secondQuantity)
        composeRule.onNodeWithTag(WORK_RECORD_SAVE_BUTTON_TEST_TAG)
            .performScrollTo()
            .performClick()

        val context = composeRule.activity
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(
                context.getString(R.string.home_tab_daily_records)
            ).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(styleName, substring = true)
                .fetchSemanticsNodes().size >= 2
        }
        assertTrue(
            composeRule.onAllNodesWithText(styleName, substring = true)
                .fetchSemanticsNodes().size >= 2
        )
        assertTrue(
            composeRule.onAllNodesWithText("¥$firstAmount").fetchSemanticsNodes().isNotEmpty()
        )
        assertTrue(
            composeRule.onAllNodesWithText("¥$secondAmount").fetchSemanticsNodes().isNotEmpty()
        )

        composeRule.onNodeWithText(context.getString(R.string.home_tab_month_stats)).performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(styleName).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("¥$totalAmount").assertIsDisplayed()
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

    private fun calculateAmount(unitPrice: String, quantity: String): String {
        return BigDecimal(unitPrice)
            .multiply(BigDecimal(quantity))
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString()
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
