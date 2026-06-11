package com.example.alarm_map.repositorio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm_map.modelo.Alarme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para RepositorioAlarme usando Robolectric.
 * Testa todas as operações CRUD com banco SQLite real na JVM.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RepositorioAlarmeTest {

    private lateinit var repositorio: RepositorioAlarme
    private lateinit var contexto: Context

    // Alarme padrão reutilizado nos testes
    private val alarmePadrao = Alarme(
        nome = "Casa",
        latitude = -23.5505,
        longitude = -46.6333,
        raioMetros = 200,
        ativo = true,
        apenasVibrar = false
    )

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
        // Deleta o banco entre testes para isolamento total
        contexto.deleteDatabase("alarmes.db")
        repositorio = RepositorioAlarme(contexto)
    }

    // --- inserir ---

    @Test
    fun `inserir retorna ID positivo`() {
        val id = repositorio.inserir(alarmePadrao)
        assertTrue("ID deve ser positivo, foi $id", id > 0)
    }

    @Test
    fun `inserir dois alarmes retorna IDs diferentes`() {
        val id1 = repositorio.inserir(alarmePadrao)
        val id2 = repositorio.inserir(alarmePadrao.copy(nome = "Trabalho"))
        assertTrue(id1 > 0)
        assertTrue(id2 > 0)
        assertFalse("IDs devem ser diferentes", id1 == id2)
    }

    // --- listarTodos ---

    @Test
    fun `listarTodos retorna lista vazia quando nao ha alarmes`() {
        assertTrue(repositorio.listarTodos().isEmpty())
    }

    @Test
    fun `listarTodos retorna alarme inserido`() {
        repositorio.inserir(alarmePadrao)
        val lista = repositorio.listarTodos()
        assertEquals(1, lista.size)
        val alarme = lista.first()
        assertEquals("Casa", alarme.nome)
        assertEquals(-23.5505, alarme.latitude, 0.0001)
        assertEquals(-46.6333, alarme.longitude, 0.0001)
        assertEquals(200, alarme.raioMetros)
        assertTrue(alarme.ativo)
        assertFalse(alarme.apenasVibrar)
    }

    @Test
    fun `listarTodos retorna todos os alarmes inseridos`() {
        repositorio.inserir(alarmePadrao)
        repositorio.inserir(alarmePadrao.copy(nome = "Trabalho"))
        repositorio.inserir(alarmePadrao.copy(nome = "Academia"))
        assertEquals(3, repositorio.listarTodos().size)
    }

    @Test
    fun `listarTodos retorna em ordem decrescente de ID`() {
        repositorio.inserir(alarmePadrao.copy(nome = "Primeiro"))
        repositorio.inserir(alarmePadrao.copy(nome = "Segundo"))
        val lista = repositorio.listarTodos()
        assertEquals("Segundo", lista[0].nome)
        assertEquals("Primeiro", lista[1].nome)
    }

    // --- listarAtivos ---

    @Test
    fun `listarAtivos retorna apenas alarmes ativos`() {
        repositorio.inserir(alarmePadrao.copy(nome = "Ativo", ativo = true))
        repositorio.inserir(alarmePadrao.copy(nome = "Inativo", ativo = false))
        val ativos = repositorio.listarAtivos()
        assertEquals(1, ativos.size)
        assertEquals("Ativo", ativos.first().nome)
    }

    @Test
    fun `listarAtivos retorna lista vazia quando nenhum alarme ativo`() {
        repositorio.inserir(alarmePadrao.copy(ativo = false))
        assertTrue(repositorio.listarAtivos().isEmpty())
    }

    @Test
    fun `listarAtivos retorna todos quando todos estao ativos`() {
        repositorio.inserir(alarmePadrao.copy(nome = "A", ativo = true))
        repositorio.inserir(alarmePadrao.copy(nome = "B", ativo = true))
        assertEquals(2, repositorio.listarAtivos().size)
    }

    // --- atualizar ---

    @Test
    fun `atualizar muda nome do alarme`() {
        val id = repositorio.inserir(alarmePadrao)
        val alarmeAtualizado = alarmePadrao.copy(id = id, nome = "Casa Nova")
        val linhasAfetadas = repositorio.atualizar(alarmeAtualizado)
        assertEquals(1, linhasAfetadas)
        val alarme = repositorio.listarTodos().first()
        assertEquals("Casa Nova", alarme.nome)
    }

    @Test
    fun `atualizar muda raio do alarme`() {
        val id = repositorio.inserir(alarmePadrao)
        val alarmeAtualizado = alarmePadrao.copy(id = id, raioMetros = 500)
        repositorio.atualizar(alarmeAtualizado)
        assertEquals(500, repositorio.listarTodos().first().raioMetros)
    }

    @Test
    fun `atualizar muda apenasVibrar`() {
        val id = repositorio.inserir(alarmePadrao.copy(apenasVibrar = false))
        val alarmeAtualizado = alarmePadrao.copy(id = id, apenasVibrar = true)
        repositorio.atualizar(alarmeAtualizado)
        assertTrue(repositorio.listarTodos().first().apenasVibrar)
    }

    @Test
    fun `atualizar com ID inexistente retorna 0 linhas afetadas`() {
        val linhasAfetadas = repositorio.atualizar(alarmePadrao.copy(id = 9999L))
        assertEquals(0, linhasAfetadas)
    }

    // --- alternarAtivo ---

    @Test
    fun `alternarAtivo desativa alarme ativo`() {
        val id = repositorio.inserir(alarmePadrao.copy(ativo = true))
        val linhas = repositorio.alternarAtivo(id, false)
        assertEquals(1, linhas)
        assertFalse(repositorio.listarTodos().first().ativo)
    }

    @Test
    fun `alternarAtivo ativa alarme inativo`() {
        val id = repositorio.inserir(alarmePadrao.copy(ativo = false))
        repositorio.alternarAtivo(id, true)
        assertTrue(repositorio.listarTodos().first().ativo)
    }

    @Test
    fun `alternarAtivo com ID inexistente retorna 0 linhas afetadas`() {
        val linhas = repositorio.alternarAtivo(9999L, false)
        assertEquals(0, linhas)
    }

    // --- deletar ---

    @Test
    fun `deletar remove o alarme do banco`() {
        val id = repositorio.inserir(alarmePadrao)
        val linhas = repositorio.deletar(id)
        assertEquals(1, linhas)
        assertTrue(repositorio.listarTodos().isEmpty())
    }

    @Test
    fun `deletar apenas o alarme especificado`() {
        val id1 = repositorio.inserir(alarmePadrao.copy(nome = "A"))
        repositorio.inserir(alarmePadrao.copy(nome = "B"))
        repositorio.deletar(id1)
        val lista = repositorio.listarTodos()
        assertEquals(1, lista.size)
        assertEquals("B", lista.first().nome)
    }

    @Test
    fun `deletar com ID inexistente retorna 0 linhas afetadas`() {
        val linhas = repositorio.deletar(9999L)
        assertEquals(0, linhas)
    }

    // --- integridade dos dados ---

    @Test
    fun `alarme inserido preserva todos os campos corretamente`() {
        val alarmeCompleto = Alarme(
            nome = "Teste Completo",
            latitude = -15.7801,
            longitude = -47.9292,
            raioMetros = 350,
            ativo = false,
            apenasVibrar = true
        )
        val id = repositorio.inserir(alarmeCompleto)
        assertNotNull(id)

        val recuperado = repositorio.listarTodos().first()
        assertEquals("Teste Completo", recuperado.nome)
        assertEquals(-15.7801, recuperado.latitude, 0.0001)
        assertEquals(-47.9292, recuperado.longitude, 0.0001)
        assertEquals(350, recuperado.raioMetros)
        assertFalse(recuperado.ativo)
        assertTrue(recuperado.apenasVibrar)
    }
}
