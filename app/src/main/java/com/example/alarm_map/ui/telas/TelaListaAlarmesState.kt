package com.example.alarm_map.ui.telas

import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme

/**
 * Gerenciador de estado da tela de lista de alarmes.
 * Extrai a lógica de negócio do composable para permitir testes unitários
 * com cobertura rastreável pelo JaCoCo/SonarQube.
 */
class TelaListaAlarmesState(
    private val repositorio: RepositorioAlarme
) {
    private val _alarmes = mutableListOf<Alarme>()
    val alarmes: List<Alarme> get() = _alarmes.toList()

    var exibirDialogoTema: Boolean = false
        private set

    /** Carrega todos os alarmes do repositório. */
    fun carregarAlarmes() {
        _alarmes.clear()
        _alarmes.addAll(repositorio.listarTodos())
    }

    /** Alterna o estado ativo/inativo de um alarme. */
    fun alternarAtivo(alarmeId: Long, novoEstado: Boolean) {
        repositorio.alternarAtivo(alarmeId, novoEstado)
        val indice = _alarmes.indexOfFirst { it.id == alarmeId }
        if (indice >= 0) {
            _alarmes[indice] = _alarmes[indice].copy(ativo = novoEstado)
        }
    }

    /** Deleta um alarme pelo ID. */
    fun deletarAlarme(alarmeId: Long) {
        repositorio.deletar(alarmeId)
        _alarmes.removeAll { it.id == alarmeId }
    }

    /** Abre o diálogo de configuração de tema. */
    fun abrirDialogoTema() {
        exibirDialogoTema = true
    }

    /** Fecha o diálogo de configuração de tema. */
    fun fecharDialogoTema() {
        exibirDialogoTema = false
    }
}
