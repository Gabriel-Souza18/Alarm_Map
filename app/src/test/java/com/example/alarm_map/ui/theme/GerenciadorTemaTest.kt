package com.example.alarm_map.ui.theme

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para GerenciadorTema usando Robolectric.
 * Robolectric simula o Android na JVM — sem emulador, sem dispositivo.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GerenciadorTemaTest {

    private lateinit var gerenciador: GerenciadorTema
    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
        // Limpa as preferências antes de cada teste para garantir isolamento
        contexto.getSharedPreferences("config_temas", Context.MODE_PRIVATE)
            .edit().clear().commit()
        gerenciador = GerenciadorTema(contexto)
    }

    // --- Testes de TemaModo ---

    @Test
    fun `obterModo retorna SISTEMA por padrao`() {
        assertEquals(TemaModo.SISTEMA, gerenciador.obterModo())
    }

    @Test
    fun `salvarModo ESCURO e obterModo retorna ESCURO`() {
        gerenciador.salvarModo(TemaModo.ESCURO)
        assertEquals(TemaModo.ESCURO, gerenciador.obterModo())
    }

    @Test
    fun `salvarModo CLARO e obterModo retorna CLARO`() {
        gerenciador.salvarModo(TemaModo.CLARO)
        assertEquals(TemaModo.CLARO, gerenciador.obterModo())
    }

    @Test
    fun `salvarModo SISTEMA e obterModo retorna SISTEMA`() {
        gerenciador.salvarModo(TemaModo.ESCURO)
        gerenciador.salvarModo(TemaModo.SISTEMA)
        assertEquals(TemaModo.SISTEMA, gerenciador.obterModo())
    }

    @Test
    fun `obterModo com valor corrompido retorna SISTEMA`() {
        // Grava um valor inválido diretamente nas prefs para simular dado corrompido
        contexto.getSharedPreferences("config_temas", Context.MODE_PRIVATE)
            .edit().putString("modo_tema", "VALOR_INVALIDO").commit()
        assertEquals(TemaModo.SISTEMA, gerenciador.obterModo())
    }

    @Test
    fun `obterModo persiste apos recriar o gerenciador`() {
        gerenciador.salvarModo(TemaModo.ESCURO)
        val novoGerenciador = GerenciadorTema(contexto)
        assertEquals(TemaModo.ESCURO, novoGerenciador.obterModo())
    }

    // --- Testes de TemaCor ---

    @Test
    fun `obterCor retorna ROXO por padrao`() {
        assertEquals(TemaCor.ROXO, gerenciador.obterCor())
    }

    @Test
    fun `salvarCor AZUL e obterCor retorna AZUL`() {
        gerenciador.salvarCor(TemaCor.AZUL)
        assertEquals(TemaCor.AZUL, gerenciador.obterCor())
    }

    @Test
    fun `salvarCor VERDE e obterCor retorna VERDE`() {
        gerenciador.salvarCor(TemaCor.VERDE)
        assertEquals(TemaCor.VERDE, gerenciador.obterCor())
    }

    @Test
    fun `salvarCor VERMELHO e obterCor retorna VERMELHO`() {
        gerenciador.salvarCor(TemaCor.VERMELHO)
        assertEquals(TemaCor.VERMELHO, gerenciador.obterCor())
    }

    @Test
    fun `salvarCor LARANJA e obterCor retorna LARANJA`() {
        gerenciador.salvarCor(TemaCor.LARANJA)
        assertEquals(TemaCor.LARANJA, gerenciador.obterCor())
    }

    @Test
    fun `obterCor com valor corrompido retorna ROXO`() {
        contexto.getSharedPreferences("config_temas", Context.MODE_PRIVATE)
            .edit().putString("cor_tema", "COR_INEXISTENTE").commit()
        assertEquals(TemaCor.ROXO, gerenciador.obterCor())
    }

    @Test
    fun `obterCor persiste apos recriar o gerenciador`() {
        gerenciador.salvarCor(TemaCor.LARANJA)
        val novoGerenciador = GerenciadorTema(contexto)
        assertEquals(TemaCor.LARANJA, novoGerenciador.obterCor())
    }

    @Test
    fun `salvar modo e cor independentemente nao interfere um no outro`() {
        gerenciador.salvarModo(TemaModo.ESCURO)
        gerenciador.salvarCor(TemaCor.VERDE)
        assertEquals(TemaModo.ESCURO, gerenciador.obterModo())
        assertEquals(TemaCor.VERDE, gerenciador.obterCor())
    }
}
