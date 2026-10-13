package com.canbustool.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.canbustool.app.state.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionBar(
    connectionState: ConnectionState,
    socketCanInterfaces: List<String>,
    selectedSocketCanInterface: String,
    onSocketCanInterfaceSelected: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val connected = connectionState is ConnectionState.Connected
    val connecting = connectionState is ConnectionState.Connecting

    Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EnumDropdown(
            label = "Interface",
            options = socketCanInterfaces.ifEmpty { listOf(selectedSocketCanInterface) },
            selected = selectedSocketCanInterface,
            optionLabel = { it },
            enabled = !connected && !connecting,
            onSelected = onSocketCanInterfaceSelected,
        )
        Spacer(Modifier.width(12.dp))
        if (connected || connecting) {
            Button(onClick = onDisconnect, enabled = !connecting) { Text(if (connecting) "Connecting…" else "Disconnect") }
        } else {
            Button(onClick = onConnect) { Text("Connect") }
        }
        Spacer(Modifier.width(16.dp))
        val statusText = when (connectionState) {
            is ConnectionState.Disconnected -> "Disconnected"
            is ConnectionState.Connecting -> "Connecting…"
            is ConnectionState.Connected -> "Connected: ${connectionState.label}"
            is ConnectionState.Error -> "Error: ${connectionState.message}"
        }
        val color = if (connectionState is ConnectionState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        Text(statusText, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    enabled: Boolean,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = optionLabel(selected),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).width(200.dp),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
