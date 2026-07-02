package com.example.alarm_map.ui.telas

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.alarm_map.banco.BancoAlarmes
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.ui.theme.TemaCor
import com.example.alarm_map.ui.theme.TemaModo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelaListaAlarmesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
        contexto.deleteDatabase("alarmes.db")
    }

    @Test
    fun testTelaListaAlarmesVazia() {
        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { _, _ -> }
            )
        }

        // Verifica se a tela exibe o texto de estado vazio
        composeTestRule.onNodeWithText("Nenhum alarme cadastrado.\nToque em + para adicionar.").assertExists()
    }

    @Test
    fun testTelaListaAlarmesComItens() {
        val dbHelper = BancoAlarmes(contexto)
        val db = dbHelper.writableDatabase
        db.execSQL(
            "INSERT INTO alarmes (nome, latitude, longitude, raio_metros, ativo, apenas_vibrar) VALUES (?, ?, ?, ?, ?, ?)",
            arrayOf("Trabalho", -23.55, -46.63, 300, 1, 0)
        )
        db.close()

        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 1,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.CLARO,
                corTema = TemaCor.AZUL,
                aoAlterarTema = { _, _ -> }
            )
        }

        // Verifica se o alarme cadastrado é exibido
        composeTestRule.onNodeWithText("Trabalho").assertExists()
        composeTestRule.onNodeWithText("Raio: 300 m").assertExists()
        composeTestRule.onNodeWithText("Tocar e vibrar").assertExists()
    }

    @Test
    fun testExibirDialogoDeTema() {
        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { _, _ -> }
            )
        }

        // Clica no ícone de configurações de tema
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()

        // Verifica se o diálogo abriu
        composeTestRule.onNodeWithText("Personalizar Tema").assertExists()
        composeTestRule.onNodeWithText("Modo de Exibição").assertExists()
        composeTestRule.onNodeWithText("Cor do Aplicativo").assertExists()

        // Clica em Cancelar para fechar
        composeTestRule.onNodeWithText("Cancelar").performClick()
        composeTestRule.onNodeWithText("Personalizar Tema").assertDoesNotExist()
    }

    @Test
    fun testSalvarNovoTema() {
        var temaAlterado = false
        var modoSalvo: TemaModo? = null
        var corSalva: TemaCor? = null

        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { modo, cor ->
                    temaAlterado = true
                    modoSalvo = modo
                    corSalva = cor
                }
            )
        }

        // Abre diálogo
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()

        // Seleciona modo "Claro"
        composeTestRule.onNodeWithText("Claro").performClick()
        
        // Clica em "Salvar"
        composeTestRule.onNodeWithText("Salvar").performClick()

        // Verifica se o callback foi acionado corretamente
        assertTrue(temaAlterado)
        assertEquals(TemaModo.CLARO, modoSalvo)
    }

    // --- Testes adicionais do diálogo de configuração de tema ---

    @Test
    fun testDialogoConfiguracaoTemaExibeModos() {
        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { _, _ -> }
            )
        }

        // Abre o diálogo de configuração de tema
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()

        // Verifica se todos os botões de modo de exibição estão presentes
        composeTestRule.onNodeWithText("Sistema").assertExists()
        composeTestRule.onNodeWithText("Claro").assertExists()
        composeTestRule.onNodeWithText("Escuro").assertExists()
    }

    @Test
    fun testDialogoConfiguracaoTemaExibeCores() {
        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { _, _ -> }
            )
        }

        // Abre o diálogo de configuração de tema
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()

        // Verifica se a seção de cores está visível
        composeTestRule.onNodeWithText("Cor do Aplicativo").assertExists()

        // Verifica que a cor selecionada exibe o ícone de check
        composeTestRule.onNodeWithContentDescription("Selecionado").assertExists()
    }

    @Test
    fun testDialogoConfiguracaoTemaSalvar() {
        var callbackChamado = false
        var modoRecebido: TemaModo? = null
        var corRecebida: TemaCor? = null

        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { modo, cor ->
                    callbackChamado = true
                    modoRecebido = modo
                    corRecebida = cor
                }
            )
        }

        // Abre o diálogo e clica em Salvar sem alterar nada
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()
        composeTestRule.onNodeWithText("Salvar").performClick()

        // Verifica se o callback foi chamado com os valores atuais
        assertTrue(callbackChamado)
        assertEquals(TemaModo.SISTEMA, modoRecebido)
        assertEquals(TemaCor.ROXO, corRecebida)

        // Verifica que o diálogo foi fechado
        composeTestRule.onNodeWithText("Personalizar Tema").assertDoesNotExist()
    }

    @Test
    fun testDialogoConfiguracaoTemaCancelar() {
        var callbackChamado = false

        composeTestRule.setContent {
            TelaListaAlarmes(
                chaveDeRecarga = 0,
                aoIrParaMapa = {},
                aoEditarAlarme = {},
                modoTema = TemaModo.SISTEMA,
                corTema = TemaCor.ROXO,
                aoAlterarTema = { _, _ ->
                    callbackChamado = true
                }
            )
        }

        // Abre o diálogo
        composeTestRule.onNodeWithContentDescription("Configurações de Tema").performClick()
        composeTestRule.onNodeWithText("Personalizar Tema").assertExists()

        // Clica em Cancelar
        composeTestRule.onNodeWithText("Cancelar").performClick()

        // Verifica que o diálogo foi fechado e o callback NÃO foi chamado
        composeTestRule.onNodeWithText("Personalizar Tema").assertDoesNotExist()
        assertFalse(callbackChamado)
    }
}
