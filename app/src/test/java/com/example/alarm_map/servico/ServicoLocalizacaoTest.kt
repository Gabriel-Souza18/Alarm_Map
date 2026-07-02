package com.example.alarm_map.servico

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ServicoLocalizacaoTest {

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testServicoCriacao() {
        // Inicializa o serviço e verifica se foi criado corretamente
        val controller = Robolectric.buildService(ServicoLocalizacao::class.java)
        val servico = controller.create().get()
        assertNotNull(servico)
        controller.destroy()
    }

    @Test
    fun testPararAlarmeSemPlayer() {
        // Garante que pararAlarmeAtual não lança exceções quando não há som rodando
        ServicoLocalizacao.pararAlarmeAtual(contexto)
    }
}
