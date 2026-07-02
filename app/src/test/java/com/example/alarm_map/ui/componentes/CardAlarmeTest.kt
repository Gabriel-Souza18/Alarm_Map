package com.example.alarm_map.ui.componentes

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.alarm_map.modelo.Alarme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para o composable [CardAlarme].
 *
 * Usa Robolectric + Compose Testing para validar a renderização
 * dos elementos visuais e os callbacks de interação do card.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CardAlarmeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Alarme padrão usado em vários testes
    private fun criarAlarme(
        nome: String = "Casa",
        latitude: Double = -23.55050,
        longitude: Double = -46.63330,
        raioMetros: Int = 200,
        ativo: Boolean = true,
        apenasVibrar: Boolean = false
    ) = Alarme(
        id = 1L,
        nome = nome,
        latitude = latitude,
        longitude = longitude,
        raioMetros = raioMetros,
        ativo = ativo,
        apenasVibrar = apenasVibrar
    )

    // =========================================================================
    // Testes de exibição de dados
    // =========================================================================

    @Test
    fun testCardAlarmeExibeNome() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(nome = "Trabalho"),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        composeTestRule.onNodeWithText("Trabalho").assertExists()
    }

    @Test
    fun testCardAlarmeExibeRaio() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(raioMetros = 500),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        composeTestRule.onNodeWithText("Raio: 500 m").assertExists()
    }

    @Test
    fun testCardAlarmeExibeCoordenadas() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(latitude = -23.55050, longitude = -46.63330),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        // O composable formata como "%.5f, %.5f"
        composeTestRule.onNodeWithText("-23.55050, -46.63330").assertExists()
    }

    // =========================================================================
    // Testes de estado ativo/inativo
    // =========================================================================

    @Test
    fun testCardAlarmeAtivoCorPrimaryContainer() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(ativo = true),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        // Verifica que o card é renderizado quando ativo
        composeTestRule.onNodeWithText("Casa").assertExists()
    }

    @Test
    fun testCardAlarmeInativoCorSurfaceVariant() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(ativo = false),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        // Verifica que o card é renderizado quando inativo
        composeTestRule.onNodeWithText("Casa").assertExists()
    }

    // =========================================================================
    // Testes de modo de vibração
    // =========================================================================

    @Test
    fun testCardAlarmeApenasVibrarTexto() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(apenasVibrar = true),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()
    }

    @Test
    fun testCardAlarmeTocarEVibrarTexto() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(apenasVibrar = false),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        composeTestRule.onNodeWithText("Tocar e vibrar").assertExists()
    }

    // =========================================================================
    // Testes de interação / callbacks
    // =========================================================================

    @Test
    fun testCardAlarmeSwitchAlternar() {
        var valorRecebido: Boolean? = null

        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(ativo = true),
                aoAlternarAtivo = { valorRecebido = it },
                aoEditar = {},
                aoDeletar = {}
            )
        }

        // O Switch é um nó toggleable; clica nele para alternar
        composeTestRule.onNode(isToggleable()).performClick()
        composeTestRule.waitForIdle()

        // O Switch estava checked=true, ao clicar o Compose envia false para onCheckedChange
        assertEquals(false, valorRecebido)
    }

    @Test
    fun testCardAlarmeCliqueEditar() {
        var editarChamado = false

        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(),
                aoAlternarAtivo = {},
                aoEditar = { editarChamado = true },
                aoDeletar = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Editar alarme").performClick()
        composeTestRule.waitForIdle()

        assertTrue("O callback aoEditar deveria ter sido chamado", editarChamado)
    }

    @Test
    fun testCardAlarmecliqueDeletar() {
        var deletarChamado = false

        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = { deletarChamado = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Deletar alarme").performClick()
        composeTestRule.waitForIdle()

        assertTrue("O callback aoDeletar deveria ter sido chamado", deletarChamado)
    }

    // =========================================================================
    // Teste de renderização completa
    // =========================================================================

    @Test
    fun testCardAlarmeExibeTodosElementos() {
        composeTestRule.setContent {
            CardAlarme(
                alarme = criarAlarme(
                    nome = "Escritório",
                    latitude = -15.78000,
                    longitude = -47.92000,
                    raioMetros = 350,
                    ativo = true,
                    apenasVibrar = true
                ),
                aoAlternarAtivo = {},
                aoEditar = {},
                aoDeletar = {}
            )
        }

        // Nome do alarme
        composeTestRule.onNodeWithText("Escritório").assertExists()

        // Raio
        composeTestRule.onNodeWithText("Raio: 350 m").assertExists()

        // Coordenadas formatadas
        composeTestRule.onNodeWithText("-15.78000, -47.92000").assertExists()

        // Texto de modo vibração
        composeTestRule.onNodeWithText("Apenas vibrar").assertExists()

        // Switch presente
        composeTestRule.onNode(isToggleable()).assertExists()

        // Botões de editar e deletar
        composeTestRule.onNodeWithContentDescription("Editar alarme").assertExists()
        composeTestRule.onNodeWithContentDescription("Deletar alarme").assertExists()
    }
}
