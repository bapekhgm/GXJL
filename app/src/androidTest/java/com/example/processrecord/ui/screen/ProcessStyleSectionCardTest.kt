package com.example.processrecord.ui.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.processrecord.MainActivity
import com.example.processrecord.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@SdkSuppress(maxSdkVersion = 35)
class ProcessStyleSectionCardTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun processSelectorOverlayClick_opensAddProcessMenu() {
        composeRule.onNodeWithTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithTag(ADD_PROCESS_MENU_ITEM_TEST_TAG)
            .assertIsDisplayed()
            .performClick()

        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun addProcessDialog_duplicateName_disablesConfirmButton() {
        val context = composeRule.activity
        val processName = "UiTestProc${System.currentTimeMillis()}"

        openAddProcessDialogFromSelector()
        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG).performTextInput(processName)
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextInput("1")
        composeRule.onNodeWithTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG).performClick()

        openAddProcessDialogFromSelector()
        val duplicateInput = processName.lowercase(Locale.getDefault())
        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG).performTextInput(duplicateInput)
        composeRule.onNodeWithTag(PROCESS_DIALOG_PRICE_INPUT_TEST_TAG).performTextInput("2")

        composeRule.onNodeWithText(
            context.getString(R.string.process_exists_message, duplicateInput)
        ).assertIsDisplayed()
        composeRule.onNodeWithTag(PROCESS_DIALOG_CONFIRM_BUTTON_TEST_TAG).assertIsNotEnabled()
    }

    private fun openAddProcessDialogFromSelector() {
        composeRule.onNodeWithTag(PROCESS_SELECTOR_OVERLAY_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(ADD_PROCESS_MENU_ITEM_TEST_TAG)
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithTag(PROCESS_DIALOG_NAME_INPUT_TEST_TAG)
            .assertIsDisplayed()
    }
}
