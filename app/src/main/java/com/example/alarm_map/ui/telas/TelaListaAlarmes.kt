package com.example.alarm_map.ui.telas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme
import com.example.alarm_map.ui.componentes.CardAlarme

/**
 * Tela principal com a lista de alarmes cadastrados.
 *
 * @param chaveDeRecarga   Incrementado pela MainActivity ao voltar do mapa, forçando recarga
 * @param aoIrParaMapa     Chamado ao criar novo alarme (sem parâmetro)
 * @param aoEditarAlarme   Chamado ao editar um alarme existente (passa o alarme)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaAlarmes(
    chaveDeRecarga: Int,
    aoIrParaMapa: () -> Unit,
    aoEditarAlarme: (Alarme) -> Unit
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioAlarme(contexto) }

    // Lista observável de alarmes — recarrega sempre que chaveDeRecarga muda
    val alarmes = remember { mutableStateListOf<Alarme>() }

    LaunchedEffect(chaveDeRecarga) {
        alarmes.clear()
        alarmes.addAll(repositorio.listarTodos())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Alarmes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = aoIrParaMapa,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo alarme",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingInterno ->

        if (alarmes.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum alarme cadastrado.\nToque em + para adicionar.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(top = 8.dp)
            ) {
                items(
                    items = alarmes,
                    key = { alarme -> alarme.id }
                ) { alarme ->
                    CardAlarme(
                        alarme = alarme,
                        aoAlternarAtivo = { novoEstado ->
                            repositorio.alternarAtivo(alarme.id, novoEstado)
                            val indice = alarmes.indexOfFirst { it.id == alarme.id }
                            if (indice >= 0) {
                                alarmes[indice] = alarme.copy(ativo = novoEstado)
                            }
                        },
                        aoEditar = {
                            aoEditarAlarme(alarme)
                        },
                        aoDeletar = {
                            repositorio.deletar(alarme.id)
                            alarmes.removeIf { it.id == alarme.id }
                        }
                    )
                }
            }
        }
    }
}
