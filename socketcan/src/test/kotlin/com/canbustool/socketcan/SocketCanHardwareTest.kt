package com.canbustool.socketcan

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Needs a live interface; skipped unless `CAN_TEST_IFACE` names one (e.g. `can0`, `vcan0`).
 * `CAN_TEST_SEND=1` additionally transmits one frame, which needs another node to acknowledge
 * it on a real bus (vcan needs none).
 */
class SocketCanHardwareTest {
    private val iface = System.getenv("CAN_TEST_IFACE")

    @Test
    fun opensAndListens() {
        if (iface == null) return
        assertTrue(iface in SocketCanInterfaces.list(), "$iface not listed")
        SocketCanConnection.open(iface).use { conn ->
            val seconds = System.getenv("CAN_TEST_LISTEN_SECONDS")?.toLong() ?: 5
            val received = runBlocking { withTimeoutOrNull(seconds * 1000) { conn.readFlow().take(10).toList() } }
            println("SocketCAN $iface: received ${received?.size ?: 0}+ frames in ${seconds}s; first: ${received?.firstOrNull()}")
            if (System.getenv("CAN_TEST_SEND") == "1") {
                val sent = conn.write(0x123, extended = false, remote = false, data = byteArrayOf(1, 2, 3, 4))
                println("sent: $sent")
            }
        }
    }
}
