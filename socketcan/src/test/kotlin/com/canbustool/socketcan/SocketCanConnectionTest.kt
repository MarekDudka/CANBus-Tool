package com.canbustool.socketcan

import kotlin.test.Test
import kotlin.test.assertFailsWith

class SocketCanConnectionTest {
    @Test
    fun openingMissingInterfaceFailsCleanly() {
        assertFailsWith<SocketCanException> { SocketCanConnection.open("nonexistent0") }
    }
}
