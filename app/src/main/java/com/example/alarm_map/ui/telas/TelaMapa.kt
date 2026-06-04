package com.example.alarm_map.ui.telas

import android.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import java.net.URL
import kotlin.math.roundToInt

/**
 * Tela do mapa para criar um novo alarme.
 * Centraliza o mapa na localização atual do usuário ao abrir.
 * O usuário pode tocar no mapa ou buscar por endereço para definir a localização,
 * e ajustar o raio com um Slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaMapa(aoVoltar: () -> Unit) {
    val contexto = LocalContext.current
    val escopo = rememberCoroutineScope()
    val controladorTeclado = LocalSoftwareKeyboardController.current
    val repositorio = remember { RepositorioAlarme(contexto) }
    val estadoBottomSheet = rememberBottomSheetScaffoldState()

    // Estado da tela
    var nomeBusca by remember { mutableStateOf("") }
    var nomeAlarme by remember { mutableStateOf("") }
    var raioMetros by remember { mutableFloatStateOf(200f) }
    var pontoSelecionado by remember { mutableStateOf<GeoPoint?>(null) }

    // Referências mutáveis ao mapa e overlays para atualização dinâmica
    var mapaView: MapView? by remember { mutableStateOf(null) }
    var marcadorAtual: Marker? by remember { mutableStateOf(null) }
    var circuloAtual: Polygon? by remember { mutableStateOf(null) }

    // Ao abrir a tela, centraliza o mapa na posição atual do usuário
    LaunchedEffect(Unit) {
        try {
            val clienteLocalizacao = LocationServices.getFusedLocationProviderClient(contexto)
            clienteLocalizacao.lastLocation.addOnSuccessListener { localizacao ->
                localizacao?.let {
                    val ponto = GeoPoint(it.latitude, it.longitude)
                    mapaView?.controller?.apply {
                        animateTo(ponto)
                        setZoom(16.0)
                    }
                }
            }
        } catch (e: Exception) {
            // Permissão não concedida ainda — mantém posição padrão
        }
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
                    mapaView?.controller?.apply {
                        animateTo(GeoPoint(it.latitude, it.longitude))
                        setZoom(16.0)
                    }
                } ?: Toast.makeText(contexto, "Localização ainda não disponível", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(contexto, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
        }
    }

    // Busca coordenadas de um endereço via Nominatim (OpenStreetMap, gratuito)
    fun buscarEndereco() {
        if (nomeBusca.isBlank()) return
        controladorTeclado?.hide()
        escopo.launch {
            try {
                val endereçoCodificado = java.net.URLEncoder.encode(nomeBusca, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$endereçoCodificado&format=json&limit=1"
                val resposta = withContext(Dispatchers.IO) {
                    val conexao = URL(url).openConnection() as java.net.HttpURLConnection
                    conexao.setRequestProperty("User-Agent", "AlarmMapApp/1.0")
                    conexao.inputStream.bufferedReader().readText()
                }
                val json = JSONArray(resposta)
                if (json.length() > 0) {
                    val resultado = json.getJSONObject(0)
                    val lat = resultado.getDouble("lat")
                    val lon = resultado.getDouble("lon")
                    val ponto = GeoPoint(lat, lon)
                    colocarMarcador(ponto)
                    mapaView?.controller?.setZoom(16.0)
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(contexto, "Endereço não encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(contexto, "Erro na busca: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = estadoBottomSheet,
        sheetPeekHeight = 220.dp,
        topBar = {
            TopAppBar(
                title = { Text("Novo Alarme") },
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

                // Botão salvar
                Button(
                    onClick = {
                        val ponto = pontoSelecionado
                        when {
                            ponto == null ->
                                Toast.makeText(contexto, "Toque no mapa para escolher a localização", Toast.LENGTH_SHORT).show()
                            nomeAlarme.isBlank() ->
                                Toast.makeText(contexto, "Digite um nome para o alarme", Toast.LENGTH_SHORT).show()
                            else -> {
                                val alarme = Alarme(
                                    nome = nomeAlarme.trim(),
                                    latitude = ponto.latitude,
                                    longitude = ponto.longitude,
                                    raioMetros = raioMetros.roundToInt()
                                )
                                repositorio.inserir(alarme)
                                Toast.makeText(contexto, "Alarme salvo!", Toast.LENGTH_SHORT).show()
                                aoVoltar()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Salvar alarme")
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

            // Barra de busca flutuando sobre o mapa (topo da Box)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nomeBusca,
                        onValueChange = { nomeBusca = it },
                        label = { Text("Buscar endereço") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { buscarEndereco() })
                    )
                    IconButton(onClick = { buscarEndereco() }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
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
