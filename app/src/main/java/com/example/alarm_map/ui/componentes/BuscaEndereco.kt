package com.example.alarm_map.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.net.URL

/**
 * Resultado de busca retornado pela API Photon.
 * Contém o ponto geográfico e o endereço formatado para exibição.
 */
data class ResultadoBusca(
    val ponto: GeoPoint,
    val nome: String,
    val enderecoFormatado: String,
    val tipo: String = ""
)

/**
 * Componente de busca de endereço com autocomplete usando a API Photon (Komoot).
 *
 * Funcionalidades:
 * - Autocomplete com debounce de 400ms (evita spam de requests)
 * - Dropdown com até 5 sugestões formatadas
 * - Location bias: prioriza resultados próximos à posição atual do mapa
 * - Tolerante a erros de digitação
 *
 * @param centroMapa Posição atual do centro do mapa para location bias
 * @param aoSelecionarEndereco Callback chamado quando o usuário seleciona um resultado
 */
@Composable
fun BuscaEndereco(
    centroMapa: GeoPoint?,
    aoSelecionarEndereco: (GeoPoint, String) -> Unit,
    funcaoBusca: (String, GeoPoint?) -> List<ResultadoBusca> = { query, centro -> buscarPhoton(query, centro) }
) {
    val controladorTeclado = LocalSoftwareKeyboardController.current
    val gerenciadorFoco = LocalFocusManager.current

    var consulta by remember { mutableStateOf("") }
    var resultados by remember { mutableStateOf<List<ResultadoBusca>>(emptyList()) }
    var carregando by remember { mutableStateOf(false) }
    var mostrarResultados by remember { mutableStateOf(false) }
    var temFoco by remember { mutableStateOf(false) }
    var deSugestao by remember { mutableStateOf(false) }

    // Debounce: aguarda 400ms após o usuário parar de digitar antes de buscar
    LaunchedEffect(consulta) {
        if (deSugestao) {
            deSugestao = false
            return@LaunchedEffect
        }

        if (consulta.length < 3) {
            resultados = emptyList()
            mostrarResultados = false
            return@LaunchedEffect
        }

        carregando = true
        delay(400L) // debounce

        try {
            val novosResultados = withContext(Dispatchers.IO) {
                funcaoBusca(consulta, centroMapa)
            }
            val resultadosOrdenados = novosResultados.sortedWith { r1, r2 ->
                val p1 = obterPrioridadeTipo(r1.tipo)
                val p2 = obterPrioridadeTipo(r2.tipo)
                if (p1 != p2) {
                    p1.compareTo(p2)
                } else if (centroMapa != null) {
                    val resultadoDistancia1 = FloatArray(1)
                    android.location.Location.distanceBetween(
                        centroMapa.latitude,
                        centroMapa.longitude,
                        r1.ponto.latitude,
                        r1.ponto.longitude,
                        resultadoDistancia1
                    )
                    val resultadoDistancia2 = FloatArray(1)
                    android.location.Location.distanceBetween(
                        centroMapa.latitude,
                        centroMapa.longitude,
                        r2.ponto.latitude,
                        r2.ponto.longitude,
                        resultadoDistancia2
                    )
                    resultadoDistancia1[0].compareTo(resultadoDistancia2[0])
                } else {
                    0
                }
            }
            val topResultados = resultadosOrdenados.take(5)
            resultados = topResultados
            mostrarResultados = topResultados.isNotEmpty()
        } catch (_: Exception) {
            resultados = emptyList()
            mostrarResultados = false
        } finally {
            carregando = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Campo de busca
        OutlinedTextField(
            value = consulta,
            onValueChange = {
                deSugestao = false
                consulta = it
            },
            label = { Text("Buscar endereço") },
            placeholder = { Text("Rua, bairro, cidade…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { temFoco = it.isFocused },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (carregando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else if (consulta.isNotEmpty()) {
                    IconButton(onClick = {
                        deSugestao = false
                        consulta = ""
                        resultados = emptyList()
                        mostrarResultados = false
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // Dropdown de sugestões
        AnimatedVisibility(
            visible = mostrarResultados && temFoco,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 260.dp)
                ) {
                    items(resultados) { resultado ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    controladorTeclado?.hide()
                                    gerenciadorFoco.clearFocus()
                                    deSugestao = true
                                    consulta = resultado.nome
                                    mostrarResultados = false
                                    aoSelecionarEndereco(resultado.ponto, resultado.nome)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = resultado.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (resultado.enderecoFormatado.isNotEmpty()) {
                                    Text(
                                        text = resultado.enderecoFormatado,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Constrói a URL da API Photon com os parâmetros fornecidos.
 * Visível internamente para testes unitários.
 *
 * @param consulta Texto digitado pelo usuário
 * @param centroMapa Centro atual do mapa para location bias
 * @return URL completa para a API Photon
 */
internal fun construirUrlPhoton(consulta: String, centroMapa: GeoPoint?): String {
    val consultaCodificada = java.net.URLEncoder.encode(consulta, "UTF-8")
    val urlBuilder = StringBuilder("https://photon.komoot.io/api/?q=$consultaCodificada&limit=15")

    centroMapa?.let {
        urlBuilder.append("&lat=${it.latitude}&lon=${it.longitude}&location_bias_scale=0.6")
    }

    return urlBuilder.toString()
}

/**
 * Parseia a resposta JSON da API Photon e retorna lista de ResultadoBusca.
 * Visível internamente para testes unitários.
 *
 * @param respostaJson String JSON retornada pela API Photon (GeoJSON FeatureCollection)
 * @return Lista de resultados parseados
 */
internal fun parsearRespostaPhoton(respostaJson: String): List<ResultadoBusca> {
    val json = JSONObject(respostaJson)
    val features = json.getJSONArray("features")
    val resultados = mutableListOf<ResultadoBusca>()

    for (i in 0 until features.length()) {
        val feature = features.getJSONObject(i)
        val geometry = feature.getJSONObject("geometry")
        val coordenadas = geometry.getJSONArray("coordinates")
        // Photon retorna [longitude, latitude] (padrão GeoJSON)
        val lon = coordenadas.getDouble(0)
        val lat = coordenadas.getDouble(1)

        val props = feature.getJSONObject("properties")
        val nome = props.optString("name", "")
        val rua = props.optString("street", "")
        val cidade = props.optString("city", "")
        val estado = props.optString("state", "")
        val pais = props.optString("country", "")
        val tipo = props.optString("type", props.optString("osm_value", ""))

        // Monta o nome de exibição — usa o name se tiver, senão a rua
        val nomeExibicao = when {
            nome.isNotEmpty() -> nome
            rua.isNotEmpty() -> rua
            cidade.isNotEmpty() -> cidade
            else -> "Local sem nome"
        }

        // Monta o endereço secundário (subtítulo)
        val partes = listOfNotNull(
            rua.takeIf { it.isNotEmpty() && it != nomeExibicao },
            cidade.takeIf { it.isNotEmpty() && it != nomeExibicao },
            estado.takeIf { it.isNotEmpty() },
            pais.takeIf { it.isNotEmpty() }
        )
        val enderecoFormatado = partes.joinToString(", ")

        resultados.add(
            ResultadoBusca(
                ponto = GeoPoint(lat, lon),
                nome = nomeExibicao,
                enderecoFormatado = enderecoFormatado,
                tipo = tipo
            )
        )
    }

    return resultados
}

/**
 * Busca endereços na API Photon (Komoot) — deve ser chamada em IO dispatcher.
 */
private fun buscarPhoton(consulta: String, centroMapa: GeoPoint?): List<ResultadoBusca> {
    val url = construirUrlPhoton(consulta, centroMapa)
    val conexao = URL(url).openConnection() as java.net.HttpURLConnection
    conexao.setRequestProperty("User-Agent", "AlarmMapApp/1.0")
    conexao.connectTimeout = 5000
    conexao.readTimeout = 5000

    val resposta = conexao.inputStream.bufferedReader().readText()
    return parsearRespostaPhoton(resposta)
}

/**
 * Retorna a prioridade (relevância) do tipo do local para ordenação.
 * Menor valor indica maior prioridade (relevância).
 */
internal fun obterPrioridadeTipo(tipo: String): Int {
    return when (tipo.lowercase()) {
        "country" -> 1
        "state" -> 2
        "city", "town", "village", "hamlet", "municipality" -> 3
        "district", "suburb", "neighbourhood", "locality", "postcode" -> 4
        "street", "highway" -> 5
        else -> 6 // house, building, poi, etc.
    }
}
