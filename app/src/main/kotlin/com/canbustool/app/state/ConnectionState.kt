package com.canbustool.app.state

sealed interface ConnectionState {
    data object Disconnected : ConnectionState
    data object Connecting : ConnectionState
    data class Connected(val label: String) : ConnectionState
    data class Error(val message: String) : ConnectionState
}
