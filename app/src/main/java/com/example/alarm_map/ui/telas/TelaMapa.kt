package com.example.alarm_map.ui.telas

import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme
import com.example.alarm_map.ui.componentes.BuscaEndereco
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.roundToInt

/**
 * Tela do mapa para criar um novo alarme.
 * Centraliza o mapa na localização atual do usuário ao abrir.
 * O usuário pode tocar no mapa ou buscar por endereço para definir a localização,
 * e ajustar o raio com um Slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapa(
    alarmeParaEditar: Alarme? = null,
    aoVoltar: () -> Unit
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioAlarme(contexto) }
    val estadoBottomSheet = rememberBottomSheetScaffoldState()

    // Estado da tela
    var nomeAlarme by remember { mutableStateOf(alarmeParaEditar?.nome ?: "") }
    var raioMetros by remember { mutableFloatStateOf(alarmeParaEditar?.raioMetros?.toFloat() ?: 200f) }
    var pontoSelecionado by remember { mutableStateOf<GeoPoint?>(alarmeParaEditar?.let { GeoPoint(it.latitude, it.longitude) }) }
    var apenasVibrar by remember { mutableStateOf(alarmeParaEditar?.apenasVibrar ?: false) }

    // Referências mutáveis ao mapa e overlays para atualização dinâmica
    var mapaView: MapView? by remember { mutableStateOf(null) }
    var marcadorAtual: Marker? by remember { mutableStateOf(null) }
    var circuloAtual: Polygon? by remember { mutableStateOf(null) }
    var marcadorMinhaLocalizacao: Marker? by remember { mutableStateOf(null) }

    // Coloca ou atualiza o marcador da localização atual do usuário no mapa
    fun atualizarMarcadorMinhaLocalizacao(ponto: GeoPoint) {
        val mapa = mapaView ?: return
        marcadorMinhaLocalizacao?.let { mapa.overlays.remove(it) }
        val novoMarcador = Marker(mapa).apply {
            position = ponto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            title = "Minha localização"
            icon = androidx.core.content.ContextCompat.getDrawable(
                mapa.context,
                android.R.drawable.ic_menu_mylocation
            )?.apply {
                setTint(Color.parseColor("#2196F3"))
            }
        }
        mapa.overlays.add(novoMarcador)
        marcadorMinhaLocalizacao = novoMarcador
        mapa.invalidate()
    }

    // Atualiza o círculo de raio no mapa quando o ponto ou raio mudar
    fun atualizarCirculo(ponto: GeoPoint, raio: Float) {
        val mapa = mapaView ?: return
        circuloAtual?.let { mapa.overlays.remove(it) }
        val novoCirculo = Polygon(mapa).apply {
            fillPaint.color = Color.argb(60, 33, 150, 243)
            outlinePaint.color = Color.argb(200, 33, 150, 243)
            outlinePaint.strokeWidth = 3f
            points = Polygon.pointsAsCircle(ponto, raio.toDouble())
        }
        mapa.overlays.add(0, novoCirculo)
        circuloAtual = novoCirculo
        mapa.invalidate()
    }

    // Coloca o marcador no mapa e atualiza o estado
    fun colocarMarcador(ponto: GeoPoint) {
        val mapa = mapaView ?: return
        marcadorAtual?.let { mapa.overlays.remove(it) }
        val novoMarcador = Marker(mapa).apply {
            position = ponto
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Localização do alarme"
        }
        mapa.overlays.add(novoMarcador)
        marcadorAtual = novoMarcador
        pontoSelecionado = ponto
        atualizarCirculo(ponto, raioMetros)
        mapa.controller.animateTo(ponto)
    }

    // Centraliza o mapa na localização atual do usuário
    fun irParaMinhaLocalizacao() {
        try {
            val clienteLocalizacao = LocationServices.getFusedLocationProviderClient(contexto)
            clienteLocalizacao.lastLocation.addOnSuccessListener { localizacao ->
                localizacao?.let {
                    val ponto = GeoPoint(it.latitude, it.longitude)
                    atualizarMarcadorMinhaLocalizacao(ponto)
                    mapaView?.controller?.apply {
                        animateTo(ponto)
                        setZoom(16.0)
                    }
                } ?: Toast.makeText(contexto, "Localização ainda não disponível", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(contexto, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
        }
    }



    // Ao abrir a tela, centraliza o mapa e cria o marcador inicial se for edição,
    // ou busca a localização do usuário se for um novo alarme.
    LaunchedEffect(mapaView) {
        val mapa = mapaView ?: return@LaunchedEffect
        if (alarmeParaEditar != null) {
            val ponto = GeoPoint(alarmeParaEditar.latitude, alarmeParaEditar.longitude)
            mapa.controller.apply {
                setCenter(ponto)
                setZoom(16.0)
            }
            colocarMarcador(ponto)
        }

        // Sempre tenta obter a localização do usuário para colocar o marcador de localização atual
        try {
            val clienteLocalizacao = LocationServices.getFusedLocationProviderClient(contexto)
            clienteLocalizacao.lastLocation.addOnSuccessListener { localizacao ->
                localizacao?.let {
                    val pontoUsuario = GeoPoint(it.latitude, it.longitude)
                    atualizarMarcadorMinhaLocalizacao(pontoUsuario)
                    if (alarmeParaEditar == null) {
                        mapa.controller.apply {
                            animateTo(pontoUsuario)
                            setZoom(16.0)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Permissão não concedida ainda ou erro — mantém posição padrão
        }
    }

    BottomSheetScaffold(
        scaffoldState = estadoBottomSheet,
        sheetPeekHeight = 220.dp,
        topBar = {
            TopAppBar(
                title = { Text(if (alarmeParaEditar != null) "Editar Alarme" else "Novo Alarme") },
                navigationIcon = {
                    IconButton(onClick = aoVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        sheetContent = {
            // --- Painel inferior de configuração ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Configurar alarme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

                // Campo: nome do alarme
                OutlinedTextField(
                    value = nomeAlarme,
                    onValueChange = { nomeAlarme = it },
                    label = { Text("Nome do alarme") },
                    placeholder = { Text("Ex: Casa, Trabalho…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

                // Slider de raio
                Text(
                    text = "Raio: ${raioMetros.roundToInt()} metros",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = raioMetros,
                    onValueChange = { novoRaio ->
                        raioMetros = novoRaio
                        pontoSelecionado?.let { atualizarCirculo(it, novoRaio) }
                    },
                    valueRange = 50f..2000f,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "50 m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "2000 m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

                // Opção: Apenas vibrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Apenas vibrar",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Desativa o som e apenas vibra o celular",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Switch(
                        checked = apenasVibrar,
                        onCheckedChange = { apenasVibrar = it }
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))

                // Botão salvar
                Button(
                    onClick = {
                        val ponto = pontoSelecionado
                        when {
                            ponto == null ->
                                Toast.makeText(contexto, "Toque no mapa para escolher a localização", Toast.LENGTH_SHORT).show()
                            else -> {
                                val nomeFinal = if (nomeAlarme.isBlank()) "Alarme" else nomeAlarme.trim()
                                val alarme = if (alarmeParaEditar != null) {
                                    alarmeParaEditar.copy(
                                        nome = nomeFinal,
                                        latitude = ponto.latitude,
                                        longitude = ponto.longitude,
                                        raioMetros = raioMetros.roundToInt(),
                                        apenasVibrar = apenasVibrar
                                    )
                                } else {
                                    Alarme(
                                        nome = nomeFinal,
                                        latitude = ponto.latitude,
                                        longitude = ponto.longitude,
                                        raioMetros = raioMetros.roundToInt(),
                                        apenasVibrar = apenasVibrar
                                    )
                                }

                                if (alarmeParaEditar != null) {
                                    repositorio.atualizar(alarme)
                                    Toast.makeText(contexto, "Alarme atualizado!", Toast.LENGTH_SHORT).show()
                                } else {
                                    repositorio.inserir(alarme)
                                    Toast.makeText(contexto, "Alarme salvo!", Toast.LENGTH_SHORT).show()
                                }
                                aoVoltar()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (alarmeParaEditar != null) "Salvar alterações" else "Salvar alarme")
                }
            }
        }
    ) { paddingInterno ->
        // Box permite que a barra de busca flutue SOBRE o mapa, sem sobreposição indesejada
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
        ) {
            // Mapa OSMDroid — fundo da Box
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    Configuration.getInstance().userAgentValue = "AlarmMapApp/1.0"
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(14.0)
                        // Posição inicial: Brasília (fallback caso GPS não esteja disponível)
                        controller.setCenter(GeoPoint(-15.7801, -47.9292))

                        // Overlay para capturar toques no mapa
                        val receptor = object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(ponto: GeoPoint): Boolean {
                                colocarMarcador(ponto)
                                return true
                            }
                            override fun longPressHelper(ponto: GeoPoint): Boolean = false
                        }
                        overlays.add(MapEventsOverlay(receptor))
                        mapaView = this
                    }
                },
                update = { mapa -> mapaView = mapa }
            )

            // Barra de busca com autocomplete Photon flutuando sobre o mapa
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                BuscaEndereco(
                    centroMapa = mapaView?.mapCenter?.let {
                        GeoPoint(it.latitude, it.longitude)
                    },
                    aoSelecionarEndereco = { ponto, _ ->
                        colocarMarcador(ponto)
                        mapaView?.controller?.setZoom(16.0)
                    }
                )
            }

            // Botão "Minha localização" no canto inferior direito
            FloatingActionButton(
                onClick = { irParaMinhaLocalizacao() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Minha localização",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
