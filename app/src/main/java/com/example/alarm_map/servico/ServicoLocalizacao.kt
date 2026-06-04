package com.example.alarm_map.servico

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
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
 */
class ServicoLocalizacao : Service() {

    companion object {
        const val ID_CANAL = "canal_localizacao"
        const val ID_NOTIFICACAO = 1
        // Intervalo de atualização de localização em milissegundos (10 segundos)
        const val INTERVALO_MS = 10_000L
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
                    dispararAlarme()
                }
            }
        }

        // Remove da memória os alarmes cujo raio o usuário saiu
        alarmeJaDisparou.retainAll(idsNaFaixa)
    }

    // --- Alarme (som + vibração) ---

    private fun dispararAlarme() {
        tocarSom()
        vibrar()
    }

    private fun tocarSom() {
        try {
            val uriSom = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val toque = RingtoneManager.getRingtone(this, uriSom)
            toque.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrar() {
        val vibrador = getSystemService(Vibrator::class.java)
        // Padrão: vibra 500ms, pausa 200ms, vibra 500ms
        val padrao = longArrayOf(0, 500, 200, 500)
        vibrador.vibrate(
            VibrationEffect.createWaveform(padrao, -1)
        )
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
