package com.example.alarm_map

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.alarm_map.servico.ServicoLocalizacao
import com.example.alarm_map.ui.telas.TelaAlarmeDisparado
import com.example.alarm_map.ui.theme.Alarm_mapTheme

/**
 * Activity exibida em tela cheia quando o usuário entra no raio de um alarme.
 * Aparece mesmo com o celular bloqueado.
 * Envia um broadcast para o ServicoLocalizacao parar o som ao desligar.
 */
class ActivityAlarmeDisparado : ComponentActivity() {

    companion object {
        const val EXTRA_NOME_ALARME = "nome_alarme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Exibe a tela mesmo com o celular bloqueado e acende a tela
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val nomeAlarme = intent.getStringExtra(EXTRA_NOME_ALARME) ?: "Alarme"

        setContent {
            Alarm_mapTheme {
                TelaAlarmeDisparado(
                    nomeAlarme = nomeAlarme,
                    aoDesligar = {
                        // Pede ao serviço para parar o som e a vibração
                        ServicoLocalizacao.pararAlarmeAtual(this)
                        finish()
                    }
                )
            }
        }
    }

    // Garante que pressionar "voltar" não ignore o alarme sem desligar
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Não faz nada — o usuário deve pressionar "Desligar" explicitamente
    }
}
