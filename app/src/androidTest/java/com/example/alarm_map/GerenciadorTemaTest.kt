package com.example.alarm_map

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.alarm_map.ui.theme.GerenciadorTema
import com.example.alarm_map.ui.theme.TemaCor
import com.example.alarm_map.ui.theme.TemaModo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GerenciadorTemaTest {

    private lateinit var gerenciadorTema: GerenciadorTema

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = appContext.getSharedPreferences("config_temas", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        gerenciadorTema = GerenciadorTema(appContext)
    }

    @Test
    fun testValoresPadrao() {
        assertEquals(TemaModo.SISTEMA, gerenciadorTema.obterModo())
        assertEquals(TemaCor.ROXO, gerenciadorTema.obterCor())
    }

    @Test
    fun testSalvarEObterModo() {
        gerenciadorTema.salvarModo(TemaModo.ESCURO)
        assertEquals(TemaModo.ESCURO, gerenciadorTema.obterModo())

        gerenciadorTema.salvarModo(TemaModo.CLARO)
        assertEquals(TemaModo.CLARO, gerenciadorTema.obterModo())
    }

    @Test
    fun testSalvarEObterCor() {
        gerenciadorTema.salvarCor(TemaCor.AZUL)
        assertEquals(TemaCor.AZUL, gerenciadorTema.obterCor())

        gerenciadorTema.salvarCor(TemaCor.VERDE)
        assertEquals(TemaCor.VERDE, gerenciadorTema.obterCor())
    }
}
