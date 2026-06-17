package com.example.alarm_map.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.alarm_map.modelo.Alarme
import com.example.alarm_map.repositorio.RepositorioAlarme
import com.example.alarm_map.ui.componentes.CardAlarme
import com.example.alarm_map.ui.theme.TemaCor
import com.example.alarm_map.ui.theme.TemaModo

/**
 * Tela principal com a lista de alarmes cadastrados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaListaAlarmes(
    chaveDeRecarga: Int,
    aoIrParaMapa: () -> Unit,
    aoEditarAlarme: (Alarme) -> Unit,
    modoTema: TemaModo,
    corTema: TemaCor,
    aoAlterarTema: (TemaModo, TemaCor) -> Unit
) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioAlarme(contexto) }
    val state = remember { TelaListaAlarmesState(repositorio) }

    // Lista observável de alarmes — recarrega sempre que chaveDeRecarga muda
    val alarmes = remember { mutableStateListOf<Alarme>() }
    var exibirDialogoTema by remember { mutableStateOf(false) }

    LaunchedEffect(chaveDeRecarga) {
        state.carregarAlarmes()
        alarmes.clear()
        alarmes.addAll(state.alarmes)
    }

    if (exibirDialogoTema) {
        DialogoConfiguracaoTema(
            modoAtual = modoTema,
            corAtual = corTema,
            aoSalvar = aoAlterarTema,
            aoFechar = { exibirDialogoTema = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Alarmes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { exibirDialogoTema = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações de Tema",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
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
                            state.alternarAtivo(alarme.id, novoEstado)
                            val indice = alarmes.indexOfFirst { it.id == alarme.id }
                            if (indice >= 0) {
                                alarmes[indice] = alarme.copy(ativo = novoEstado)
                            }
                        },
                        aoEditar = {
                            aoEditarAlarme(alarme)
                        },
                        aoDeletar = {
                            state.deletarAlarme(alarme.id)
                            alarmes.removeIf { it.id == alarme.id }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DialogoConfiguracaoTema(
    modoAtual: TemaModo,
    corAtual: TemaCor,
    aoSalvar: (TemaModo, TemaCor) -> Unit,
    aoFechar: () -> Unit
) {
    var modoSelecionado by remember { mutableStateOf(modoAtual) }
    var corSelecionada by remember { mutableStateOf(corAtual) }

    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text("Personalizar Tema") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Modo de Exibição", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                SeletorModoTema(modoSelecionado = modoSelecionado, aoSelecionar = { modoSelecionado = it })

                Text("Cor do Aplicativo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                SeletorCorTema(corSelecionada = corSelecionada, aoSelecionar = { corSelecionada = it })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    aoSalvar(modoSelecionado, corSelecionada)
                    aoFechar()
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun SeletorModoTema(
    modoSelecionado: TemaModo,
    aoSelecionar: (TemaModo) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TemaModo.values().forEach { modo ->
            val selecionado = modoSelecionado == modo
            Button(
                onClick = { aoSelecionar(modo) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selecionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selecionado) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = when (modo) {
                        TemaModo.SISTEMA -> "Sistema"
                        TemaModo.CLARO -> "Claro"
                        TemaModo.ESCURO -> "Escuro"
                    }
                )
            }
        }
    }
}

@Composable
private fun SeletorCorTema(
    corSelecionada: TemaCor,
    aoSelecionar: (TemaCor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TemaCor.values().forEach { cor ->
            val selecionada = corSelecionada == cor
            val corBorda = if (selecionada) MaterialTheme.colorScheme.onSurface else Color.Transparent
            val checkmarkTint = if (cor == TemaCor.LARANJA) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(cor.corPrincipal)
                    .clickable { aoSelecionar(cor) }
                    .border(
                        width = if (selecionada) 3.dp else 0.dp,
                        color = corBorda,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selecionada) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selecionado",
                        tint = checkmarkTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
