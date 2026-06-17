package com.example.alarm_map

import androidx.compose.ui.graphics.Color
import com.example.alarm_map.ui.theme.TemaCor
import com.example.alarm_map.ui.theme.TemaModo
import com.example.alarm_map.ui.theme.obterColorScheme
import com.example.alarm_map.ui.theme.ajustarBrilho
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    @Test
    fun testObterColorSchemeClaro() {
        TemaCor.values().forEach { temaCor ->
            val colorScheme = obterColorScheme(temaCor, darkTheme = false)
            assertEquals(temaCor.corPrincipal, colorScheme.primary)
            // No modo claro, background deve ser F5F5F5
            assertEquals(Color(0xFFF5F5F5), colorScheme.background)
            // No modo claro, onPrimary deve ser White
            assertEquals(Color.White, colorScheme.onPrimary)
        }
    }

    @Test
    fun testObterColorSchemeEscuro() {
        TemaCor.values().forEach { temaCor ->
            val colorScheme = obterColorScheme(temaCor, darkTheme = true)
            assertEquals(temaCor.corPrincipal, colorScheme.primary)
            // No modo escuro, background deve ser 121212
            assertEquals(Color(0xFF121212), colorScheme.background)
            // No modo escuro, onPrimary deve ser Black
            assertEquals(Color.Black, colorScheme.onPrimary)
        }
    }

    @Test
    fun testAjustarBrilho() {
        val corOriginal = Color(red = 0.5f, green = 0.5f, blue = 0.5f, alpha = 1.0f)
        
        // Diminui o brilho pela metade
        val corEscura = ajustarBrilho(corOriginal, 0.5f)
        assertEquals(0.25f, corEscura.red, 0.001f)
        assertEquals(0.25f, corEscura.green, 0.001f)
        assertEquals(0.25f, corEscura.blue, 0.001f)
        assertEquals(1.0f, corEscura.alpha, 0.001f)

        // Limita ao máximo (1.0)
        val corBrilhante = ajustarBrilho(corOriginal, 3.0f)
        assertEquals(1.0f, corBrilhante.red, 0.001f)
        assertEquals(1.0f, corBrilhante.green, 0.001f)
        assertEquals(1.0f, corBrilhante.blue, 0.001f)

        // Limita ao mínimo (0.0) com fator negativo
        val corNegativa = ajustarBrilho(corOriginal, -1.0f)
        assertEquals(0.0f, corNegativa.red, 0.001f)
        assertEquals(0.0f, corNegativa.green, 0.001f)
        assertEquals(0.0f, corNegativa.blue, 0.001f)
    }
}
