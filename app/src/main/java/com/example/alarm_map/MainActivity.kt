package com.example.alarm_map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.servico.ServicoLocalizacao
import com.example.alarm_map.ui.telas.TelaListaAlarmes
import com.example.alarm_map.ui.telas.TelaMapa
import com.example.alarm_map.ui.theme.Alarm_mapTheme

/**
 * Ponto de entrada do aplicativo.
 * Gerencia permissões de localização e a navegação entre telas.
 */
class MainActivity : ComponentActivity() {

    // Telas disponíveis para navegação simples sem biblioteca externa
    private enum class Tela { LISTA, MAPA }

    // Launcher para solicitar múltiplas permissões ao sistema
    private val solicitarPermissoes = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissoes ->
        val localizacaoAprovada = permissoes[Manifest.permission.ACCESS_FINE_LOCATION] == true
            || permissoes[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (localizacaoAprovada) {
            iniciarServico()
        } else {
            Toast.makeText(
                this,
                "Permissão de localização necessária para o app funcionar.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verificarPermissoes()

        setContent {
            Alarm_mapTheme {
                var telaAtual by remember { mutableStateOf(Tela.LISTA) }
                var alarmeParaEditar by remember { mutableStateOf<Alarme?>(null) }
                var chaveDeRecarga by remember { mutableStateOf(0) }

                when (telaAtual) {
                    Tela.LISTA -> TelaListaAlarmes(
                        chaveDeRecarga = chaveDeRecarga,
                        aoIrParaMapa = {
                            alarmeParaEditar = null
                            telaAtual = Tela.MAPA
                        },
                        aoEditarAlarme = { alarme ->
                            alarmeParaEditar = alarme
                            telaAtual = Tela.MAPA
                        }
                    )
                    Tela.MAPA -> TelaMapa(
                        alarmeParaEditar = alarmeParaEditar,
                        aoVoltar = {
                            alarmeParaEditar = null
                            chaveDeRecarga++
                            telaAtual = Tela.LISTA
                        }
                    )
                }
            }
        }
    }

    private fun verificarPermissoes() {
        val permissoesFinas = Manifest.permission.ACCESS_FINE_LOCATION
        val permissoesGrossas = Manifest.permission.ACCESS_COARSE_LOCATION

        val faltam = listOf(permissoesFinas, permissoesGrossas).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (faltam.isEmpty()) {
            iniciarServico()
        } else {
            solicitarPermissoes.launch(faltam.toTypedArray())
        }
    }

    private fun iniciarServico() {
        val intencao = Intent(this, ServicoLocalizacao::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intencao)
        } else {
            startService(intencao)
        }
    }
}