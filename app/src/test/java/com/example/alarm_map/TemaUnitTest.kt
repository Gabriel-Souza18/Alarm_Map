package com.example.alarm_map

import com.example.alarm_map.ui.theme.TemaCor
import com.example.alarm_map.ui.theme.TemaModo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TemaUnitTest {

    @Test
    fun testTemaModoValores() {
        val valores = TemaModo.values()
        assertEquals(3, valores.size)
        assertEquals(TemaModo.SISTEMA, TemaModo.valueOf("SISTEMA"))
        assertEquals(TemaModo.CLARO, TemaModo.valueOf("CLARO"))
        assertEquals(TemaModo.ESCURO, TemaModo.valueOf("ESCURO"))
    }

    @Test
    fun testTemaCorValores() {
        val valores = TemaCor.values()
        assertEquals(5, valores.size)

        valores.forEach { cor ->
            assertNotNull(cor.nome)
            assertNotNull(cor.corPrincipal)
        }

        assertEquals(TemaCor.ROXO, TemaCor.valueOf("ROXO"))
        assertEquals("Roxo", TemaCor.ROXO.nome)
    }
}
