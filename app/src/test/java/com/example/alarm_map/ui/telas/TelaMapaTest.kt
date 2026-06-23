package com.example.alarm_map.ui.telas

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.alarm_map.modelo.Alarme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelaMapaTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
        contexto.deleteDatabase("alarmes.db")
    }

    @Test
    fun testTelaMapaLayoutNovoAlarme() {
        var voltou = false
        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = null,
                aoVoltar = { voltou = true }
            )
        }

        // Verifica se elementos básicos do layout do bottom sheet e top bar são exibidos
        composeTestRule.onNodeWithText("Novo Alarme").assertExists()
        composeTestRule.onNodeWithText("Configurar alarme").assertExists()
        composeTestRule.onNodeWithText("Nome do alarme").assertExists()
        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()
        composeTestRule.onNodeWithText("Salvar alarme").assertExists()

        // Testa o clique em voltar
        composeTestRule.onNodeWithContentDescription("Voltar").performClick()
        assertTrue(voltou)
    }

    @Test
    fun testTelaMapaEdicaoAlarme() {
        val alarme = Alarme(
            id = 1,
            nome = "Alarme Teste Edicao",
            latitude = -23.55,
            longitude = -46.63,
            raioMetros = 500,
            ativo = true,
            apenasVibrar = true
        )

        composeTestRule.setContent {
            TelaMapa(
                alarmeParaEditar = alarme,
                aoVoltar = {}
            )
        }

        composeTestRule.onNodeWithText("Editar Alarme").assertExists()
        composeTestRule.onNodeWithText("Alarme Teste Edicao").assertExists()
        composeTestRule.onNodeWithText("Salvar alterações").assertExists()
    }
}
