package com.canbustool.core

import kotlinx.coroutines.flow.Flow

/** An open connection to one CAN bus, independent of which backend provides it. */
interface CanConnection : AutoCloseable {
    /**
     * Writes a classic CAN frame and returns it as a [CanMessage] stamped with the time since the
     * connection was opened, for the caller to show/record as a transmitted entry.
     */
    fun write(id: Long, extended: Boolean, remote: Boolean, data: ByteArray): CanMessage

    /** Emits received frames until the collector is cancelled; throws if the bus/driver fails. */
    fun readFlow(): Flow<CanMessage>
}
