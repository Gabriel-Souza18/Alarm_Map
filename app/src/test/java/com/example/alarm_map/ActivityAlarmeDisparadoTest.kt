package com.example.alarm_map

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ActivityAlarmeDisparadoTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ActivityAlarmeDisparado>()

    @Test
    fun testAlarmeDisparadoUI() {
        // Verifica se a tela de alarme disparado exibe o nome do alarme e o botão
        composeTestRule.onNodeWithText("Você chegou!").assertExists()
        composeTestRule.onNodeWithText("Desligar alarme").assertExists()
    }

    @Test
    fun testCliqueDesligarFinalizaActivity() {
        composeTestRule.onNodeWithText("Desligar alarme").performClick()
        composeTestRule.waitForIdle()
        assertTrue(composeTestRule.activity.isFinishing)
    }
}
