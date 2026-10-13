package com.canbustool.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

private const val SETUP_COMMAND =
    "sudo modprobe peak_usb && sudo ip link set can0 up type can bitrate 500000 && ip -br link show can0"

@Composable
fun CanSetupDialog(onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
            Column(modifier = Modifier.padding(24.dp).widthIn(min = 480.dp)) {
                Text("Set up a CAN interface", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    "CANBus-Tool talks to Linux SocketCAN, so the adapter must show up as a CAN interface " +
                        "that is up. For a PEAK PCAN-USB (or any adapter with a mainline kernel driver), " +
                        "run this in a terminal:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                    SelectionContainer {
                        Text(
                            SETUP_COMMAND,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "modprobe peak_usb loads the driver, ip link … up starts can0 at 500 kBit/s " +
                        "(use your bus's bitrate), and the last command should list can0 as UP. " +
                        "The bitrate is set by the system, not by this app. Then pick can0 in the " +
                        "Interface list and press Connect.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(SETUP_COMMAND)) }) { Text("Copy command") }
                    Button(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}
