package com.example.alarm_map.servico

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.alarm_map.ActivityAlarmeDisparado
import com.example.alarm_map.R
import com.example.alarm_map.repositorio.RepositorioAlarme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Serviço em primeiro plano que monitora a localização do usuário continuamente.
 * Para cada atualização, verifica se o dispositivo entrou no raio de algum alarme ativo.
 * Quando disparado, toca som em loop e exibe uma tela de desligar ao usuário.
 */
class ServicoLocalizacao : Service() {

    companion object {
        const val ID_CANAL = "canal_localizacao"
        const val ID_NOTIFICACAO = 1
        // Intervalo de atualização de localização em milissegundos (10 segundos)
        const val INTERVALO_MS = 10_000L

        // Referência estática ao MediaPlayer para que a Activity possa pará-lo
        private var playerAlarme: MediaPlayer? = null
        private var vibradorAtivo: Vibrator? = null

        /**
         * Para o som e a vibração do alarme.
         * Chamado pela ActivityAlarmeDisparado quando o usuário pressiona "Desligar".
         */
        fun pararAlarmeAtual(contexto: Context) {
            playerAlarme?.apply {
                if (isPlaying) stop()
                release()
            }
            playerAlarme = null
            vibradorAtivo?.cancel()
            vibradorAtivo = null
        }
    }

    private lateinit var clienteLocalizacao: FusedLocationProviderClient
    private lateinit var repositorio: RepositorioAlarme

    // Conjunto de IDs de alarmes que já dispararam para a posição atual.
    // Evita que o alarme repita sem o usuário sair e voltar ao raio.
    private val alarmeJaDisparou = mutableSetOf<Long>()

    private val callbackLocalizacao = object : LocationCallback() {
        override fun onLocationResult(resultado: LocationResult) {
            val posicaoAtual = resultado.lastLocation ?: return
            verificarAlarmes(posicaoAtual)
        }
    }

    override fun onCreate() {
        super.onCreate()
        clienteLocalizacao = LocationServices.getFusedLocationProviderClient(this)
        repositorio = RepositorioAlarme(this)
        criarCanalDeNotificacao()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(ID_NOTIFICACAO, criarNotificacao())
        iniciarMonitoramento()
        return START_STICKY // reinicia automaticamente se o sistema matar o serviço
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        clienteLocalizacao.removeLocationUpdates(callbackLocalizacao)
        pararAlarmeAtual(this)
    }

    // --- Localização ---

    @SuppressWarnings("MissingPermission")
    private fun iniciarMonitoramento() {
        val requisicao = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, INTERVALO_MS)
            .setMinUpdateIntervalMillis(INTERVALO_MS / 2)
            .build()

        clienteLocalizacao.requestLocationUpdates(
            requisicao,
            callbackLocalizacao,
            Looper.getMainLooper()
        )
    }

    private fun verificarAlarmes(posicaoAtual: Location) {
        val alarmesAtivos = repositorio.listarAtivos()
        val idsNaFaixa = mutableSetOf<Long>()

        for (alarme in alarmesAtivos) {
            val distancia = FloatArray(1)
            Location.distanceBetween(
                posicaoAtual.latitude,
                posicaoAtual.longitude,
                alarme.latitude,
                alarme.longitude,
                distancia
            )

            val dentroDoRaio = distancia[0] <= alarme.raioMetros

            if (dentroDoRaio) {
                idsNaFaixa.add(alarme.id)
                // Só dispara uma vez por "entrada" no raio
                if (!alarmeJaDisparou.contains(alarme.id)) {
                    alarmeJaDisparou.add(alarme.id)
                    dispararAlarme(alarme.nome, alarme.apenasVibrar)
                }
            }
        }

        // Remove da memória os alarmes cujo raio o usuário saiu
        alarmeJaDisparou.retainAll(idsNaFaixa)
    }

    // --- Alarme (som em loop + vibração + tela de desligar) ---

    private fun dispararAlarme(nomeAlarme: String, apenasVibrar: Boolean) {
        if (!apenasVibrar) {
            tocarSomEmLoop()
        }
        vibrar()
        abrirTelaDeDesligar(nomeAlarme)
    }

    private fun tocarSomEmLoop() {
        try {
            // Para qualquer som anterior antes de tocar o novo
            pararAlarmeAtual(this)

            val uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            playerAlarme = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(applicationContext, uriSom)
                isLooping = true // Toca em loop até o usuário desligar
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrar() {
        try {
            val vibrador = getSystemService(Vibrator::class.java)
            vibradorAtivo = vibrador
            // Padrão de vibração: vibra 500ms, pausa 300ms, repete indefinidamente (índice 0)
            val padrao = longArrayOf(0, 500, 300)
            vibrador.vibrate(
                VibrationEffect.createWaveform(padrao, 0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Abre a tela de desligar o alarme em primeiro plano,
     * mesmo que o app esteja fechado ou o celular bloqueado.
     */
    private fun abrirTelaDeDesligar(nomeAlarme: String) {
        val intencao = Intent(this, ActivityAlarmeDisparado::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
            putExtra(ActivityAlarmeDisparado.EXTRA_NOME_ALARME, nomeAlarme)
        }
        startActivity(intencao)
    }

    // --- Notificação persistente (obrigatória para ForegroundService) ---

    private fun criarCanalDeNotificacao() {
        val canal = NotificationChannel(
            ID_CANAL,
            "Monitoramento de localização",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitora sua localização para disparar alarmes"
        }
        val gerenciador = getSystemService(NotificationManager::class.java)
        gerenciador.createNotificationChannel(canal)
    }

    private fun criarNotificacao(): Notification {
        return NotificationCompat.Builder(this, ID_CANAL)
            .setContentTitle("AlarmMap ativo")
            .setContentText("Monitorando sua localização…")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
