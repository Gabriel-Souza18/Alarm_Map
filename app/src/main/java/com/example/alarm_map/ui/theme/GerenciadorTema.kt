package com.example.alarm_map.ui.theme

import android.content.Context
import android.content.SharedPreferences

class GerenciadorTema(contexto: Context) {
    private val prefs: SharedPreferences = contexto.getSharedPreferences("config_temas", Context.MODE_PRIVATE)

    companion object {
        private const val CHAVE_MODO = "modo_tema"
        private const val CHAVE_COR = "cor_tema"
    }

    fun obterModo(): TemaModo {
        val nome = prefs.getString(CHAVE_MODO, TemaModo.SISTEMA.name) ?: TemaModo.SISTEMA.name
        return try {
            TemaModo.valueOf(nome)
        } catch (e: Exception) {
            TemaModo.SISTEMA
        }
    }

    fun salvarModo(modo: TemaModo) {
        prefs.edit().putString(CHAVE_MODO, modo.name).apply()
    }

    fun obterCor(): TemaCor {
        val nome = prefs.getString(CHAVE_COR, TemaCor.ROXO.name) ?: TemaCor.ROXO.name
        return try {
            TemaCor.valueOf(nome)
        } catch (e: Exception) {
            TemaCor.ROXO
        }
    }

    fun salvarCor(cor: TemaCor) {
        prefs.edit().putString(CHAVE_COR, cor.name).apply()
    }
}
