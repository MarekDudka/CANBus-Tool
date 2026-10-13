package com.canbustool.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.canbustool.app.state.AppViewModel
import com.canbustool.app.ui.AboutDialog
import com.canbustool.app.ui.AppScreen
import com.canbustool.app.ui.CanSetupDialog

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CANBus-Tool",
        state = WindowState(width = 1600.dp, height = 850.dp),
    ) {
        var showAbout by remember { mutableStateOf(false) }
        var showSetup by remember { mutableStateOf(false) }

        MenuBar {
            Menu("File") {
                Item("Exit") { exitApplication() }
            }
            Menu("Help") {
                Item("Set up CAN interface…") { showSetup = true }
                Item("About CANBus-Tool…") { showAbout = true }
            }
        }

        MaterialTheme {
            val scope = rememberCoroutineScope()
            val viewModel = remember { AppViewModel(scope) }
            LaunchedEffect(Unit) { viewModel.refreshAvailableChannels() }
            AppScreen(viewModel)

            if (showSetup) {
                CanSetupDialog(onDismiss = { showSetup = false })
            }
            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }
    }
}
