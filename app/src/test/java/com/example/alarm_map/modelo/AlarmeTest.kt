package com.example.alarm_map.modelo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes unitários para o modelo Alarme (data class).
 * Não requer Android — roda na JVM pura.
 */
class AlarmeTest {

    @Test
    fun `valores padrao estao corretos`() {
        val alarme = Alarme(nome = "Casa", latitude = -23.5, longitude = -46.6)
        assertEquals(0L, alarme.id)
        assertEquals(200, alarme.raioMetros)
        assertTrue(alarme.ativo)
        assertFalse(alarme.apenasVibrar)
    }

    @Test
    fun `construtor com todos os campos`() {
        val alarme = Alarme(
            id = 42L,
            nome = "Trabalho",
            latitude = -23.561,
            longitude = -46.655,
            raioMetros = 500,
            ativo = false,
            apenasVibrar = true
        )
        assertEquals(42L, alarme.id)
        assertEquals("Trabalho", alarme.nome)
        assertEquals(-23.561, alarme.latitude, 0.0001)
        assertEquals(-46.655, alarme.longitude, 0.0001)
        assertEquals(500, alarme.raioMetros)
        assertFalse(alarme.ativo)
        assertTrue(alarme.apenasVibrar)
    }

    @Test
    fun `copy altera apenas os campos especificados`() {
        val original = Alarme(id = 1L, nome = "Casa", latitude = -23.5, longitude = -46.6)
        val copia = original.copy(nome = "Casa Nova", raioMetros = 300)

        assertEquals(1L, copia.id)
        assertEquals("Casa Nova", copia.nome)
        assertEquals(-23.5, copia.latitude, 0.0001)
        assertEquals(300, copia.raioMetros)
        assertTrue(copia.ativo)
    }

    @Test
    fun `equals retorna true para alarmes identicos`() {
        val a1 = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0)
        val a2 = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0)
        assertEquals(a1, a2)
    }

    @Test
    fun `equals retorna false para alarmes diferentes`() {
        val a1 = Alarme(id = 1L, nome = "A", latitude = 0.0, longitude = 0.0)
        val a2 = Alarme(id = 2L, nome = "B", latitude = 0.0, longitude = 0.0)
        assertNotEquals(a1, a2)
    }

    @Test
    fun `hashCode é igual para alarmes identicos`() {
        val a1 = Alarme(id = 5L, nome = "X", latitude = 1.0, longitude = 2.0)
        val a2 = Alarme(id = 5L, nome = "X", latitude = 1.0, longitude = 2.0)
        assertEquals(a1.hashCode(), a2.hashCode())
    }

    @Test
    fun `toString contem o nome do alarme`() {
        val alarme = Alarme(nome = "Mercado", latitude = 0.0, longitude = 0.0)
        assertTrue(alarme.toString().contains("Mercado"))
    }

    @Test
    fun `ativo pode ser alterado via copy`() {
        val ativo = Alarme(nome = "A", latitude = 0.0, longitude = 0.0, ativo = true)
        val inativo = ativo.copy(ativo = false)
        assertFalse(inativo.ativo)
        assertTrue(ativo.ativo)
    }

    @Test
    fun `apenasVibrar pode ser alterado via copy`() {
        val semVibrar = Alarme(nome = "A", latitude = 0.0, longitude = 0.0, apenasVibrar = false)
        val comVibrar = semVibrar.copy(apenasVibrar = true)
        assertTrue(comVibrar.apenasVibrar)
        assertFalse(semVibrar.apenasVibrar)
    }
}
