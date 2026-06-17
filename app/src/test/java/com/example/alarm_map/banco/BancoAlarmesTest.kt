package com.example.alarm_map.banco

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class BancoAlarmesTest {

    private lateinit var banco: BancoAlarmes
    private lateinit var mockContext: Context
    private lateinit var mockDb: SQLiteDatabase

    @Before
    fun setUp() {
        mockContext = mock()
        mockDb = mock()
        banco = BancoAlarmes(mockContext)
    }

    @Test
    fun `onCreate executa SQL de criacao de tabela`() {
        banco.onCreate(mockDb)
        
        argumentCaptor<String>().apply {
            verify(mockDb).execSQL(capture())
            val sql = firstValue
            assertTrue(sql.contains("CREATE TABLE"))
            assertTrue(sql.contains(BancoAlarmes.TABELA))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_ID))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_NOME))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_LATITUDE))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_LONGITUDE))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_RAIO_METROS))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_ATIVO))
            assertTrue(sql.contains(BancoAlarmes.COLUNA_APENAS_VIBRAR))
        }
    }

    @Test
    fun `onUpgrade de versao 1 para 2 adiciona coluna apenas_vibrar`() {
        banco.onUpgrade(mockDb, 1, 2)
        verify(mockDb).execSQL(eq("ALTER TABLE alarmes ADD COLUMN apenas_vibrar INTEGER NOT NULL DEFAULT 0"))
    }

    @Test
    fun `onUpgrade de versao maior que 1 recria tabela`() {
        banco.onUpgrade(mockDb, 2, 3)
        verify(mockDb).execSQL(eq("DROP TABLE IF EXISTS alarmes"))
        verify(mockDb, times(2)).execSQL(any())
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
