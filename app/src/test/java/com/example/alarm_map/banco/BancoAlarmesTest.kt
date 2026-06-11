package com.example.alarm_map.banco

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Testes unitários para BancoAlarmes usando Robolectric.
 * Verifica criação de tabela e migração de versão.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class BancoAlarmesTest {

    private lateinit var contexto: Context

    @Before
    fun setUp() {
        contexto = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `banco abre sem erros e versao esta correta`() {
        val banco = BancoAlarmes(contexto)
        val db = banco.writableDatabase
        assertTrue(db.isOpen)
        assertEquals(BancoAlarmes.VERSAO, db.version)
        db.close()
    }

    @Test
    fun `onCreate cria a tabela alarmes`() {
        val banco = BancoAlarmes(contexto)
        val db = banco.readableDatabase

        // Verifica se a tabela existe via sqlite_master
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(BancoAlarmes.TABELA)
        )
        cursor.use {
            assertTrue("Tabela '${BancoAlarmes.TABELA}' deve existir", it.count > 0)
        }
        db.close()
    }

    @Test
    fun `tabela possui todas as colunas esperadas`() {
        val banco = BancoAlarmes(contexto)
        val db = banco.readableDatabase

        val cursor = db.rawQuery("PRAGMA table_info(${BancoAlarmes.TABELA})", null)
        val colunas = mutableSetOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                colunas.add(it.getString(it.getColumnIndexOrThrow("name")))
            }
        }

        assertTrue(colunas.contains(BancoAlarmes.COLUNA_ID))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_NOME))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_LATITUDE))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_LONGITUDE))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_RAIO_METROS))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_ATIVO))
        assertTrue(colunas.contains(BancoAlarmes.COLUNA_APENAS_VIBRAR))
        db.close()
    }

    @Test
    fun `onUpgrade de versao 1 para 2 adiciona coluna apenas_vibrar`() {
        val banco = BancoAlarmes(contexto)
        val db = banco.writableDatabase

        // Simula estado de versão 1: remove a coluna e recria sem ela
        db.execSQL("DROP TABLE IF EXISTS ${BancoAlarmes.TABELA}")
        db.execSQL(
            """
            CREATE TABLE ${BancoAlarmes.TABELA} (
                ${BancoAlarmes.COLUNA_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${BancoAlarmes.COLUNA_NOME} TEXT NOT NULL,
                ${BancoAlarmes.COLUNA_LATITUDE} REAL NOT NULL,
                ${BancoAlarmes.COLUNA_LONGITUDE} REAL NOT NULL,
                ${BancoAlarmes.COLUNA_RAIO_METROS} INTEGER NOT NULL DEFAULT 200,
                ${BancoAlarmes.COLUNA_ATIVO} INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        // Chama onUpgrade simulando migração 1 → 2
        banco.onUpgrade(db, 1, 2)

        // Verifica se a coluna foi adicionada
        val cursor = db.rawQuery("PRAGMA table_info(${BancoAlarmes.TABELA})", null)
        val colunas = mutableSetOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                colunas.add(it.getString(it.getColumnIndexOrThrow("name")))
            }
        }
        assertTrue(
            "Coluna '${BancoAlarmes.COLUNA_APENAS_VIBRAR}' deve existir após upgrade",
            colunas.contains(BancoAlarmes.COLUNA_APENAS_VIBRAR)
        )
        db.close()
    }

    @Test
    fun `onUpgrade de versao 2 para 3 recria a tabela`() {
        val banco = BancoAlarmes(contexto)
        val db = banco.writableDatabase

        // Simula upgrade para versão maior que 2
        banco.onUpgrade(db, 2, 3)

        // Tabela deve ainda existir (recriada)
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(BancoAlarmes.TABELA)
        )
        cursor.use {
            assertTrue("Tabela deve existir após recriar", it.count > 0)
        }
        db.close()
    }

    @Test
    fun `constantes do companion object estao corretas`() {
        assertEquals("alarmes.db", BancoAlarmes.NOME_BANCO)
        assertEquals(2, BancoAlarmes.VERSAO)
        assertEquals("alarmes", BancoAlarmes.TABELA)
        assertEquals("id", BancoAlarmes.COLUNA_ID)
        assertEquals("nome", BancoAlarmes.COLUNA_NOME)
        assertEquals("latitude", BancoAlarmes.COLUNA_LATITUDE)
        assertEquals("longitude", BancoAlarmes.COLUNA_LONGITUDE)
        assertEquals("raio_metros", BancoAlarmes.COLUNA_RAIO_METROS)
        assertEquals("ativo", BancoAlarmes.COLUNA_ATIVO)
        assertEquals("apenas_vibrar", BancoAlarmes.COLUNA_APENAS_VIBRAR)
    }
}
