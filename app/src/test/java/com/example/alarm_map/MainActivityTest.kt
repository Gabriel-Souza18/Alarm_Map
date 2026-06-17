package com.example.alarm_map

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainActivityLaunch() {
        // Verifica se o app inicializa na tela de lista com o TopBar correto
        composeTestRule.onNodeWithText("Meus Alarmes").assertExists()
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").assertExists()
        composeTestRule.onNodeWithContentDescription("Novo alarme").assertExists()
    }
}
