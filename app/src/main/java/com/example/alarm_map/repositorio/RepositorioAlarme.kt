package com.example.alarm_map.repositorio

import android.content.ContentValues
import android.content.Context
import com.example.alarm_map.banco.BancoAlarmes
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_ATIVO
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_ID
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_LATITUDE
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_LONGITUDE
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_NOME
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_RAIO_METROS
import com.example.alarm_map.banco.BancoAlarmes.Companion.COLUNA_APENAS_VIBRAR
import com.example.alarm_map.banco.BancoAlarmes.Companion.TABELA
import com.example.alarm_map.modelo.Alarme

/**
 * Responsável por todas as operações de leitura e escrita dos alarmes no banco SQLite.
 */
class RepositorioAlarme(contexto: Context) {

    private val banco = BancoAlarmes(contexto)

    /** Insere um novo alarme e retorna o ID gerado. */
    fun inserir(alarme: Alarme): Long {
        val valores = alarmeParaContentValues(alarme)
        return banco.writableDatabase.insert(TABELA, null, valores)
    }

    /** Retorna a lista de todos os alarmes, do mais recente ao mais antigo. */
    fun listarTodos(): List<Alarme> {
        val lista = mutableListOf<Alarme>()
        val cursor = banco.readableDatabase.query(
            TABELA, null, null, null, null, null, "$COLUNA_ID DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(cursorParaAlarme(it))
            }
        }
        return lista
    }

    /** Retorna apenas os alarmes que estão ativos. */
    fun listarAtivos(): List<Alarme> {
        val lista = mutableListOf<Alarme>()
        val cursor = banco.readableDatabase.query(
            TABELA, null, "$COLUNA_ATIVO = 1", null, null, null, null
        )
        cursor.use {
            while (it.moveToNext()) {
                lista.add(cursorParaAlarme(it))
            }
        }
        return lista
    }

    /** Atualiza todos os dados de um alarme existente. */
    fun atualizar(alarme: Alarme): Int {
        val valores = alarmeParaContentValues(alarme)
        return banco.writableDatabase.update(
            TABELA, valores, "$COLUNA_ID = ?", arrayOf(alarme.id.toString())
        )
    }

    /** Alterna o estado ativo/inativo de um alarme pelo ID. */
    fun alternarAtivo(id: Long, ativo: Boolean): Int {
        val valores = ContentValues().apply {
            put(COLUNA_ATIVO, if (ativo) 1 else 0)
        }
        return banco.writableDatabase.update(
            TABELA, valores, "$COLUNA_ID = ?", arrayOf(id.toString())
        )
    }

    /** Remove um alarme pelo ID. */
    fun deletar(id: Long): Int {
        return banco.writableDatabase.delete(
            TABELA, "$COLUNA_ID = ?", arrayOf(id.toString())
        )
    }

    // --- Funções auxiliares de conversão ---

    private fun alarmeParaContentValues(alarme: Alarme): ContentValues {
        return ContentValues().apply {
            put(COLUNA_NOME, alarme.nome)
            put(COLUNA_LATITUDE, alarme.latitude)
            put(COLUNA_LONGITUDE, alarme.longitude)
            put(COLUNA_RAIO_METROS, alarme.raioMetros)
            put(COLUNA_ATIVO, if (alarme.ativo) 1 else 0)
            put(COLUNA_APENAS_VIBRAR, if (alarme.apenasVibrar) 1 else 0)
        }
    }

    private fun cursorParaAlarme(cursor: android.database.Cursor): Alarme {
        return Alarme(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUNA_ID)),
            nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUNA_NOME)),
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUNA_LATITUDE)),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUNA_LONGITUDE)),
            raioMetros = cursor.getInt(cursor.getColumnIndexOrThrow(COLUNA_RAIO_METROS)),
            ativo = cursor.getInt(cursor.getColumnIndexOrThrow(COLUNA_ATIVO)) == 1,
            apenasVibrar = cursor.getInt(cursor.getColumnIndexOrThrow(COLUNA_APENAS_VIBRAR)) == 1
        )
    }
}
