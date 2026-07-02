package com.example.alarm_map.ui.telas

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes para o composable TelaAlarmeDisparado.
 * Verifica a exibição de elementos visuais e a interação com o botão de desligar.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class TelaAlarmeDisparadoTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- Testes de exibição ---

    @Test
    fun testTelaAlarmeDisparadoExibeNome() {
        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = "Trabalho",
                aoDesligar = {}
            )
        }

        // Verifica se o nome do alarme é exibido na tela
        composeTestRule.onNodeWithText("Trabalho").assertExists()
        composeTestRule.onNodeWithText("Trabalho").assertIsDisplayed()
    }

    @Test
    fun testTelaAlarmeDisparadoExibeVoceChegou() {
        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = "Casa",
                aoDesligar = {}
            )
        }

        // Verifica se a mensagem "Você chegou!" é exibida
        composeTestRule.onNodeWithText("Você chegou!").assertExists()
        composeTestRule.onNodeWithText("Você chegou!").assertIsDisplayed()
    }

    @Test
    fun testTelaAlarmeDisparadoExibeBotaoDesligar() {
        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = "Escola",
                aoDesligar = {}
            )
        }

        // Verifica se o botão "Desligar alarme" é exibido e está habilitado
        composeTestRule.onNodeWithText("Desligar alarme").assertExists()
        composeTestRule.onNodeWithText("Desligar alarme").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desligar alarme").assertHasClickAction()
    }

    // --- Testes de interação ---

    @Test
    fun testTelaAlarmeDisparadoCliqueBotao() {
        var botaoClicado = false

        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = "Mercado",
                aoDesligar = { botaoClicado = true }
            )
        }

        // Verifica que o callback ainda não foi chamado
        assertFalse(botaoClicado)

        // Clica no botão de desligar
        composeTestRule.onNodeWithText("Desligar alarme").performClick()

        // Verifica que o callback foi chamado após o clique
        assertTrue(botaoClicado)
    }

    // --- Testes de casos extremos ---

    @Test
    fun testTelaAlarmeDisparadoComNomeLongo() {
        val nomeLongo = "Este é um nome de alarme extremamente longo para testar " +
                "se a tela consegue exibir textos grandes sem quebrar o layout"

        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = nomeLongo,
                aoDesligar = {}
            )
        }

        // Verifica que o nome longo é exibido na tela
        composeTestRule.onNodeWithText(nomeLongo).assertExists()

        // Verifica que os outros elementos ainda estão presentes
        composeTestRule.onNodeWithText("Você chegou!").assertExists()
        composeTestRule.onNodeWithText("Desligar alarme").assertExists()
    }

    @Test
    fun testTelaAlarmeDisparadoComNomeVazio() {
        composeTestRule.setContent {
            TelaAlarmeDisparado(
                nomeAlarme = "",
                aoDesligar = {}
            )
        }

        // Verifica que os elementos principais ainda são exibidos com nome vazio
        composeTestRule.onNodeWithText("Você chegou!").assertExists()
        composeTestRule.onNodeWithText("Desligar alarme").assertExists()
    }
}
