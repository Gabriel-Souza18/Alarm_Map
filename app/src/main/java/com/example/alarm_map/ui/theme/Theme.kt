package com.example.alarm_map.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class TemaModo {
    SISTEMA, CLARO, ESCURO
}

enum class TemaCor(val nome: String, val corPrincipal: Color) {
    ROXO("Roxo", Color(0xFF673AB7)),
    AZUL("Azul", Color(0xFF2196F3)),
    VERDE("Verde", Color(0xFF4CAF50)),
    VERMELHO("Vermelho", Color(0xFFF44336)),
    LARANJA("Laranja", Color(0xFFFF9800))
}

private fun obterColorScheme(temaCor: TemaCor, darkTheme: Boolean): androidx.compose.material3.ColorScheme {
    val corPrincipal = temaCor.corPrincipal
    
    return if (darkTheme) {
        darkColorScheme(
            primary = corPrincipal,
            secondary = ajustarBrilho(corPrincipal, 0.8f),
            tertiary = ajustarBrilho(corPrincipal, 0.9f),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = corPrincipal,
            secondary = ajustarBrilho(corPrincipal, 0.7f),
            tertiary = ajustarBrilho(corPrincipal, 0.6f),
            background = Color(0xFFF5F5F5),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF212121),
            onSurface = Color(0xFF212121)
        )
    }
}

private fun ajustarBrilho(cor: Color, fator: Float): Color {
    return Color(
        red = (cor.red * fator).coerceIn(0f, 1f),
        green = (cor.green * fator).coerceIn(0f, 1f),
        blue = (cor.blue * fator).coerceIn(0f, 1f),
        alpha = cor.alpha
    )
}

@Composable
fun Alarm_mapTheme(
    modo: TemaModo = TemaModo.SISTEMA,
    cor: TemaCor = TemaCor.ROXO,
    content: @Composable () -> Unit
) {
    val darkTheme = when (modo) {
        TemaModo.SISTEMA -> isSystemInDarkTheme()
        TemaModo.CLARO -> false
        TemaModo.ESCURO -> true
    }

    val colorScheme = obterColorScheme(cor, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}