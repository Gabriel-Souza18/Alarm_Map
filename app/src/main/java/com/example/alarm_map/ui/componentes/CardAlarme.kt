package com.example.alarm_map.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alarm_map.modelo.Alarme

/**
 * Card que exibe as informações de um alarme na lista.
 *
 * @param alarme         Dados do alarme a exibir
 * @param aoAlternarAtivo Chamado quando o Switch é acionado
 * @param aoEditar        Chamado quando o botão de editar é pressionado
 * @param aoDeletar       Chamado quando o botão de deletar é pressionado
 */
@Composable
fun CardAlarme(
    alarme: Alarme,
    aoAlternarAtivo: (Boolean) -> Unit,
    aoEditar: () -> Unit,
    aoDeletar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarme.ativo)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ícone de localização
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (alarme.ativo)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(end = 12.dp)
            )

            // Informações do alarme
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarme.nome,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Raio: ${alarme.raioMetros} m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.5f, %.5f".format(alarme.latitude, alarme.longitude),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Switch ativo/inativo
            Switch(
                checked = alarme.ativo,
                onCheckedChange = aoAlternarAtivo
            )

            // Botão editar
            IconButton(onClick = aoEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar alarme",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Botão deletar
            IconButton(onClick = aoDeletar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Deletar alarme",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
