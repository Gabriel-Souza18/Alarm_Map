package com.example.alarm_map.ui.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ThemeComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testAlarmMapThemeSistema() {
        composeTestRule.setContent {
            Alarm_mapTheme(modo = TemaModo.SISTEMA, cor = TemaCor.ROXO) {
                Text("Sistema Theme Text")
            }
        }
        composeTestRule.onNodeWithText("Sistema Theme Text").assertExists()
    }

    @Test
    fun testAlarmMapThemeClaro() {
        composeTestRule.setContent {
            Alarm_mapTheme(modo = TemaModo.CLARO, cor = TemaCor.AZUL) {
                Text("Claro Theme Text")
            }
        }
        composeTestRule.onNodeWithText("Claro Theme Text").assertExists()
    }

    @Test
    fun testAlarmMapThemeEscuro() {
        composeTestRule.setContent {
            Alarm_mapTheme(modo = TemaModo.ESCURO, cor = TemaCor.VERDE) {
                Text("Escuro Theme Text")
            }
        }
        composeTestRule.onNodeWithText("Escuro Theme Text").assertExists()
    }
}
