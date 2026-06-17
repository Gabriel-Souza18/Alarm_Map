package com.example.alarm_map.ui.telas

import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TelaListaAlarmesStateTest {

    private lateinit var repositorio: RepositorioAlarme
    private lateinit var state: TelaListaAlarmesState

    @Before
    fun setUp() {
        repositorio = mock()
        state = TelaListaAlarmesState(repositorio)
    }

    // --- Testes de carregamento ---

    @Test
    fun `lista de alarmes inicia vazia`() {
        assertTrue(state.alarmes.isEmpty())
    }

    @Test
    fun `carregarAlarmes retorna lista vazia quando nao ha alarmes`() {
        whenever(repositorio.listarTodos()).thenReturn(emptyList())
        state.carregarAlarmes()
        assertTrue(state.alarmes.isEmpty())
    }

    @Test
    fun `carregarAlarmes retorna alarmes cadastrados`() {
        val alarme1 = Alarme(id = 1L, nome = "Casa", latitude = -23.55, longitude = -46.63)
        val alarme2 = Alarme(id = 2L, nome = "Trabalho", latitude = -23.56, longitude = -46.64)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme1, alarme2))

        state.carregarAlarmes()

        assertEquals(2, state.alarmes.size)
    }

    @Test
    fun `carregarAlarmes atualiza lista com dados corretos`() {
        val alarme = Alarme(id = 1L, nome = "Escola", latitude = -23.57, longitude = -46.65, raioMetros = 500, apenasVibrar = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))

        state.carregarAlarmes()

        val alarmeDoState = state.alarmes.first()
        assertEquals("Escola", alarmeDoState.nome)
        assertEquals(-23.57, alarmeDoState.latitude, 0.001)
        assertEquals(-46.65, alarmeDoState.longitude, 0.001)
        assertEquals(500, alarmeDoState.raioMetros)
        assertTrue(alarmeDoState.apenasVibrar)
        assertTrue(alarmeDoState.ativo)
    }

    @Test
    fun `carregarAlarmes limpa lista anterior e recarrega`() {
        val alarmeA = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0)
        val alarmeB = Alarme(id = 2L, nome = "B", latitude = 1.0, longitude = 1.0)
        whenever(repositorio.listarTodos())
            .thenReturn(listOf(alarmeA))
            .thenReturn(listOf(alarmeA, alarmeB))

        state.carregarAlarmes()
        assertEquals(1, state.alarmes.size)

        state.carregarAlarmes()
        assertEquals(2, state.alarmes.size)
    }

    @Test
    fun `carregarAlarmes com multiplos alarmes retorna na ordem correta`() {
        val alarme1 = Alarme(id = 1L, nome = "Primeiro", latitude = 0.0, longitude = 0.0)
        val alarme2 = Alarme(id = 2L, nome = "Segundo", latitude = 1.0, longitude = 1.0)
        val alarme3 = Alarme(id = 3L, nome = "Terceiro", latitude = 2.0, longitude = 2.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme3, alarme2, alarme1))

        state.carregarAlarmes()

        assertEquals(3, state.alarmes.size)
        assertEquals("Terceiro", state.alarmes[0].nome)
        assertEquals("Segundo", state.alarmes[1].nome)
        assertEquals("Primeiro", state.alarmes[2].nome)
    }

    // --- Testes de alternar ativo ---

    @Test
    fun `alternarAtivo desativa um alarme ativo`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0, ativo = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.alternarAtivo(1L, false)

        val alarmeDoState = state.alarmes.first { it.id == 1L }
        assertFalse(alarmeDoState.ativo)
    }

    @Test
    fun `alternarAtivo ativa um alarme inativo`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0, ativo = false)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.alternarAtivo(1L, true)

        val alarmeDoState = state.alarmes.first { it.id == 1L }
        assertTrue(alarmeDoState.ativo)
    }

    @Test
    fun `alternarAtivo persiste no repositorio`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0, ativo = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.alternarAtivo(1L, false)

        verify(repositorio).alternarAtivo(1L, false)
    }

    @Test
    fun `alternarAtivo com ID inexistente nao lanca excecao`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0, ativo = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.alternarAtivo(999L, false)

        assertEquals(1, state.alarmes.size)
        assertTrue(state.alarmes.first().ativo)
        verify(repositorio).alternarAtivo(999L, false)
    }

    @Test
    fun `alternarAtivo atualiza apenas o alarme correto`() {
        val alarme1 = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0, ativo = true)
        val alarme2 = Alarme(id = 2L, nome = "B", latitude = 1.0, longitude = 1.0, ativo = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme1, alarme2))
        state.carregarAlarmes()

        state.alternarAtivo(1L, false)

        val alarmeA = state.alarmes.first { it.id == 1L }
        val alarmeB = state.alarmes.first { it.id == 2L }
        assertFalse(alarmeA.ativo)
        assertTrue(alarmeB.ativo)
    }

    // --- Testes de deletar ---

    @Test
    fun `deletarAlarme remove o alarme da lista`() {
        val alarme = Alarme(id = 1L, nome = "Deletar", latitude = 0.0, longitude = 0.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()
        assertEquals(1, state.alarmes.size)

        state.deletarAlarme(1L)

        assertTrue(state.alarmes.isEmpty())
    }

    @Test
    fun `deletarAlarme persiste no repositorio`() {
        val alarme = Alarme(id = 1L, nome = "Deletar", latitude = 0.0, longitude = 0.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.deletarAlarme(1L)

        verify(repositorio).deletar(1L)
    }

    @Test
    fun `deletarAlarme remove apenas o alarme correto`() {
        val alarme1 = Alarme(id = 1L, nome = "Manter", latitude = 0.0, longitude = 0.0)
        val alarme2 = Alarme(id = 2L, nome = "Deletar", latitude = 1.0, longitude = 1.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme1, alarme2))
        state.carregarAlarmes()
        assertEquals(2, state.alarmes.size)

        state.deletarAlarme(2L)

        assertEquals(1, state.alarmes.size)
        assertEquals("Manter", state.alarmes.first().nome)
    }

    @Test
    fun `deletarAlarme com ID inexistente nao altera lista`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        state.deletarAlarme(999L)

        assertEquals(1, state.alarmes.size)
    }

    @Test
    fun `deletar todos os alarmes deixa lista vazia`() {
        val alarme1 = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0)
        val alarme2 = Alarme(id = 2L, nome = "B", latitude = 1.0, longitude = 1.0)
        val alarme3 = Alarme(id = 3L, nome = "C", latitude = 2.0, longitude = 2.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme1, alarme2, alarme3))
        state.carregarAlarmes()

        state.deletarAlarme(1L)
        state.deletarAlarme(2L)
        state.deletarAlarme(3L)

        assertTrue(state.alarmes.isEmpty())
    }

    // --- Testes de diálogo de tema ---

    @Test
    fun `dialogo de tema inicia fechado`() {
        assertFalse(state.exibirDialogoTema)
    }

    @Test
    fun `abrirDialogoTema muda estado para true`() {
        state.abrirDialogoTema()
        assertTrue(state.exibirDialogoTema)
    }

    @Test
    fun `fecharDialogoTema muda estado para false`() {
        state.abrirDialogoTema()
        assertTrue(state.exibirDialogoTema)

        state.fecharDialogoTema()
        assertFalse(state.exibirDialogoTema)
    }

    @Test
    fun `abrir e fechar dialogo multiplas vezes funciona corretamente`() {
        state.abrirDialogoTema()
        assertTrue(state.exibirDialogoTema)

        state.fecharDialogoTema()
        assertFalse(state.exibirDialogoTema)

        state.abrirDialogoTema()
        assertTrue(state.exibirDialogoTema)

        state.fecharDialogoTema()
        assertFalse(state.exibirDialogoTema)
    }

    // --- Testes de fluxo completo ---

    @Test
    fun `fluxo completo - inserir, carregar, alternar e deletar`() {
        val alarme1 = Alarme(id = 1L, nome = "Alarme 1", latitude = -23.55, longitude = -46.63, raioMetros = 300)
        val alarme2 = Alarme(id = 2L, nome = "Alarme 2", latitude = -23.56, longitude = -46.64, raioMetros = 500, apenasVibrar = true)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme1, alarme2))

        // Carrega
        state.carregarAlarmes()
        assertEquals(2, state.alarmes.size)

        // Desativa o primeiro
        state.alternarAtivo(1L, false)
        assertFalse(state.alarmes.first { it.id == 1L }.ativo)

        // Deleta o segundo
        state.deletarAlarme(2L)
        assertEquals(1, state.alarmes.size)
        assertEquals("Alarme 1", state.alarmes.first().nome)

        // Reativa o primeiro
        state.alternarAtivo(1L, true)
        assertTrue(state.alarmes.first { it.id == 1L }.ativo)
    }

    @Test
    fun `alarmes retorna copia imutavel da lista`() {
        val alarme = Alarme(id = 1L, nome = "Teste", latitude = 0.0, longitude = 0.0)
        whenever(repositorio.listarTodos()).thenReturn(listOf(alarme))
        state.carregarAlarmes()

        val lista1 = state.alarmes
        val lista2 = state.alarmes

        assertEquals(lista1, lista2)
        assertEquals(1, lista1.size)
    }
}
