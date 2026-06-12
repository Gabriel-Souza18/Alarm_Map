package com.example.alarm_map.repositorio

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.example.alarm_map.banco.BancoAlarmes
import com.example.alarm_map.modelo.Alarme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.mockito.MockedConstruction
import org.mockito.Mockito

class RepositorioAlarmeTest {

    private lateinit var repositorio: RepositorioAlarme
    private lateinit var mockContext: Context
    private lateinit var mockBanco: BancoAlarmes
    private lateinit var mockDb: SQLiteDatabase
    private lateinit var mockCursor: Cursor
    private lateinit var mockedContentValues: MockedConstruction<ContentValues>

    private val alarmePadrao = Alarme(
        id = 1L,
        nome = "Casa",
        latitude = -23.5505,
        longitude = -46.6333,
        raioMetros = 200,
        ativo = true,
        apenasVibrar = false
    )

    @Before
    fun setUp() {
        mockContext = mock()
        mockBanco = mock()
        mockDb = mock()
        mockCursor = mock()

        whenever(mockBanco.writableDatabase).thenReturn(mockDb)
        whenever(mockBanco.readableDatabase).thenReturn(mockDb)

        mockedContentValues = Mockito.mockConstruction(ContentValues::class.java)

        repositorio = RepositorioAlarme(mockContext, mockBanco)
    }

    @org.junit.After
    fun tearDown() {
        mockedContentValues.close()
    }

    @Test
    fun `inserir chama insert no SQLiteDatabase e retorna o id`() {
        whenever(mockDb.insert(any(), anyOrNull(), any())).thenReturn(42L)

        val id = repositorio.inserir(alarmePadrao)

        assertEquals(42L, id)
        verify(mockDb).insert(eq(BancoAlarmes.TABELA), anyOrNull(), any())
    }

    @Test
    fun `listarTodos retorna lista contendo os alarmes do cursor`() {
        whenever(mockDb.query(
            eq(BancoAlarmes.TABELA),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            eq("${BancoAlarmes.COLUNA_ID} DESC")
        )).thenReturn(mockCursor)

        whenever(mockCursor.moveToNext()).thenReturn(true, false)

        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_ID)).thenReturn(0)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_NOME)).thenReturn(1)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_LATITUDE)).thenReturn(2)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_LONGITUDE)).thenReturn(3)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_RAIO_METROS)).thenReturn(4)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_ATIVO)).thenReturn(5)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_APENAS_VIBRAR)).thenReturn(6)

        whenever(mockCursor.getLong(0)).thenReturn(1L)
        whenever(mockCursor.getString(1)).thenReturn("Casa")
        whenever(mockCursor.getDouble(2)).thenReturn(-23.5505)
        whenever(mockCursor.getDouble(3)).thenReturn(-46.6333)
        whenever(mockCursor.getInt(4)).thenReturn(200)
        whenever(mockCursor.getInt(5)).thenReturn(1)
        whenever(mockCursor.getInt(6)).thenReturn(0)

        val lista = repositorio.listarTodos()

        assertEquals(1, lista.size)
        val alarme = lista.first()
        assertEquals(1L, alarme.id)
        assertEquals("Casa", alarme.nome)
        assertEquals(-23.5505, alarme.latitude, 0.0001)
        assertEquals(-46.6333, alarme.longitude, 0.0001)
        assertEquals(200, alarme.raioMetros)
        assertTrue(alarme.ativo)
        assertFalse(alarme.apenasVibrar)

        verify(mockCursor).close()
    }

    @Test
    fun `listarAtivos query apenas com ativos`() {
        whenever(mockDb.query(
            eq(BancoAlarmes.TABELA),
            anyOrNull(),
            eq("${BancoAlarmes.COLUNA_ATIVO} = 1"),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )).thenReturn(mockCursor)

        whenever(mockCursor.moveToNext()).thenReturn(true, false)

        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_ID)).thenReturn(0)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_NOME)).thenReturn(1)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_LATITUDE)).thenReturn(2)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_LONGITUDE)).thenReturn(3)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_RAIO_METROS)).thenReturn(4)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_ATIVO)).thenReturn(5)
        whenever(mockCursor.getColumnIndexOrThrow(BancoAlarmes.COLUNA_APENAS_VIBRAR)).thenReturn(6)

        whenever(mockCursor.getLong(0)).thenReturn(1L)
        whenever(mockCursor.getString(1)).thenReturn("Casa")
        whenever(mockCursor.getDouble(2)).thenReturn(-23.5505)
        whenever(mockCursor.getDouble(3)).thenReturn(-46.6333)
        whenever(mockCursor.getInt(4)).thenReturn(200)
        whenever(mockCursor.getInt(5)).thenReturn(1)
        whenever(mockCursor.getInt(6)).thenReturn(0)

        val lista = repositorio.listarAtivos()
        assertEquals(1, lista.size)
        assertTrue(lista.first().ativo)
    }

    @Test
    fun `atualizar chama update no SQLiteDatabase e retorna linhas afetadas`() {
        whenever(mockDb.update(eq(BancoAlarmes.TABELA), any(), eq("${BancoAlarmes.COLUNA_ID} = ?"), any())).thenReturn(1)

        val linhas = repositorio.atualizar(alarmePadrao)

        assertEquals(1, linhas)
        verify(mockDb).update(eq(BancoAlarmes.TABELA), any(), eq("${BancoAlarmes.COLUNA_ID} = ?"), eq(arrayOf("1")))
    }

    @Test
    fun `alternarAtivo atualiza apenas coluna ativo`() {
        whenever(mockDb.update(eq(BancoAlarmes.TABELA), any(), eq("${BancoAlarmes.COLUNA_ID} = ?"), any())).thenReturn(1)

        val linhas = repositorio.alternarAtivo(1L, false)

        assertEquals(1, linhas)
        verify(mockDb).update(eq(BancoAlarmes.TABELA), any(), eq("${BancoAlarmes.COLUNA_ID} = ?"), eq(arrayOf("1")))
    }

    @Test
    fun `deletar chama delete no SQLiteDatabase`() {
        whenever(mockDb.delete(eq(BancoAlarmes.TABELA), eq("${BancoAlarmes.COLUNA_ID} = ?"), any())).thenReturn(1)

        val linhas = repositorio.deletar(1L)

        assertEquals(1, linhas)
        verify(mockDb).delete(eq(BancoAlarmes.TABELA), eq("${BancoAlarmes.COLUNA_ID} = ?"), eq(arrayOf("1")))
    }
}
