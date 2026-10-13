package com.canbustool.socketcan

import java.io.File

/** Discovers CAN network interfaces (`can0`, `vcan0`, …) via sysfs; no privileges needed. */
object SocketCanInterfaces {
    private const val ARPHRD_CAN = 280

    fun list(sysNetDir: File = File("/sys/class/net")): List<String> =
        sysNetDir.listFiles().orEmpty()
            .filter { File(it, "type").runCatching { readText().trim().toInt() }.getOrNull() == ARPHRD_CAN }
            .map { it.name }
            .sorted()
}
