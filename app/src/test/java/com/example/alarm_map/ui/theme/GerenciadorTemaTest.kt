package com.example.alarm_map.ui.theme

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class GerenciadorTemaTest {

    private lateinit var gerenciador: GerenciadorTema
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private val prefsMap = mutableMapOf<String, String>()

    @Before
    fun setUp() {
        mockContext = mock()
        mockPrefs = mock()
        mockEditor = mock()
        prefsMap.clear()

        whenever(mockContext.getSharedPreferences(eq("config_temas"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPrefs)

        whenever(mockPrefs.edit()).thenReturn(mockEditor)

        // Mock getting preferences
        whenever(mockPrefs.getString(any(), anyOrNull())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val defValue = invocation.arguments[1] as? String
            prefsMap[key] ?: defValue
        }

        // Mock putting preferences
        whenever(mockEditor.putString(any(), any())).thenAnswer { invocation ->
            val key = invocation.arguments[0] as String
            val value = invocation.arguments[1] as String
            prefsMap[key] = value
            mockEditor
        }

        // Apply just returns void, commit returns boolean
        whenever(mockEditor.apply()).then {}

        gerenciador = GerenciadorTema(mockContext)
    }

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
        prefsMap["modo_tema"] = "VALOR_INVALIDO"
        assertEquals(TemaModo.SISTEMA, gerenciador.obterModo())
    }

    @Test
    fun `obterModo persiste apos recriar o gerenciador`() {
        gerenciador.salvarModo(TemaModo.ESCURO)
        val novoGerenciador = GerenciadorTema(mockContext)
        assertEquals(TemaModo.ESCURO, novoGerenciador.obterModo())
    }

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
        prefsMap["cor_tema"] = "COR_INEXISTENTE"
        assertEquals(TemaCor.ROXO, gerenciador.obterCor())
    }

    @Test
    fun `obterCor persiste apos recriar o gerenciador`() {
        gerenciador.salvarCor(TemaCor.LARANJA)
        val novoGerenciador = GerenciadorTema(mockContext)
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
