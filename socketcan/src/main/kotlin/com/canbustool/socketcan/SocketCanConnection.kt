package com.canbustool.socketcan

import com.canbustool.core.CanConnection
import com.canbustool.core.CanMessage
import com.sun.jna.LastErrorException
import com.sun.jna.Memory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val AF_CAN = 29
private const val PF_CAN = 29
private const val SOCK_RAW = 3
private const val SOCK_NONBLOCK = 0x800
private const val CAN_RAW = 1
private const val SIOCGIFINDEX = 0x8933L
private const val SIOCGSTAMP = 0x8906L
private const val EAGAIN = 11
private const val IFNAMSIZ = 16

class SocketCanException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * A raw SocketCAN socket bound to one interface (e.g. `can0`, `vcan0`). The interface must
 * already be up — bitrate is configured by the system, not by this app, e.g.
 * `sudo ip link set can0 up type can bitrate 500000`.
 *
 * Receive timestamps come from the kernel (`SIOCGSTAMP`) and, like transmit timestamps, are
 * expressed as microseconds since [open].
 */
class SocketCanConnection private constructor(
    private val fd: Int,
    val interfaceName: String,
) : CanConnection {
    private val libc get() = LibC.INSTANCE
    private val openedAtMicros = nowMicros()

    override fun write(id: Long, extended: Boolean, remote: Boolean, data: ByteArray): CanMessage {
        val frame = CanFrameCodec.encode(id, extended, remote, data)
        val buffer = Memory(frame.size.toLong()).apply { write(0, frame, 0, frame.size) }
        try {
            if (libc.write(fd, buffer, frame.size.toLong()) != frame.size.toLong()) {
                throw SocketCanException("Short write to $interfaceName")
            }
        } catch (e: LastErrorException) {
            throw SocketCanException("Write to $interfaceName failed (errno ${e.errorCode})", e)
        }
        return CanMessage(id, extended, remote, data.copyOf(), nowMicros() - openedAtMicros)
    }

    /** Polls the non-blocking socket, so a quiet bus never ties up the caller's thread. */
    override fun readFlow(): Flow<CanMessage> = flow {
        val buffer = Memory(CanFrameCodec.FRAME_SIZE.toLong())
        val stamp = Memory(16)
        val frame = ByteArray(CanFrameCodec.FRAME_SIZE)
        while (true) {
            val count = try {
                libc.read(fd, buffer, CanFrameCodec.FRAME_SIZE.toLong())
            } catch (e: LastErrorException) {
                if (e.errorCode == EAGAIN) {
                    delay(1)
                    continue
                }
                throw SocketCanException("Read from $interfaceName failed (errno ${e.errorCode})", e)
            }
            if (count != CanFrameCodec.FRAME_SIZE.toLong()) continue // e.g. a CAN FD frame; classic CAN only
            buffer.read(0, frame, 0, frame.size)
            val micros = kernelTimestampMicros(stamp)
            CanFrameCodec.decode(frame, micros - openedAtMicros)?.let { emit(it) }
        }
    }

    /** `SIOCGSTAMP` fills a `struct timeval` (two longs); falls back to the host clock on failure. */
    private fun kernelTimestampMicros(stamp: Memory): Long = try {
        libc.ioctl(fd, SIOCGSTAMP, stamp)
        stamp.getLong(0) * 1_000_000L + stamp.getLong(8)
    } catch (e: LastErrorException) {
        nowMicros()
    }

    override fun close() {
        libc.close(fd)
    }

    companion object {
        fun open(interfaceName: String): SocketCanConnection {
            val libc = try {
                LibC.INSTANCE
            } catch (e: UnsatisfiedLinkError) {
                throw SocketCanException("SocketCAN is only available on Linux", e)
            }
            require(interfaceName.length < IFNAMSIZ) { "Interface name too long: $interfaceName" }
            val fd = try {
                libc.socket(PF_CAN, SOCK_RAW or SOCK_NONBLOCK, CAN_RAW)
            } catch (e: LastErrorException) {
                throw SocketCanException("Cannot create CAN socket (errno ${e.errorCode}); is the can_raw kernel module loaded?", e)
            }
            try {
                val ifIndex = interfaceIndex(libc, fd, interfaceName)
                val address = Memory(16).apply {
                    clear()
                    setShort(0, AF_CAN.toShort())
                    setInt(4, ifIndex)
                }
                libc.bind(fd, address, 16)
            } catch (e: LastErrorException) {
                libc.close(fd)
                throw SocketCanException("Cannot open $interfaceName (errno ${e.errorCode}); does it exist and is it up?", e)
            }
            return SocketCanConnection(fd, interfaceName)
        }

        /** `struct ifreq`: 16-byte name followed by a union whose first member is the `int` ifindex. */
        private fun interfaceIndex(libc: LibC, fd: Int, name: String): Int {
            val request = Memory(40).apply { clear() }
            request.write(0, name.toByteArray(Charsets.US_ASCII), 0, name.length)
            libc.ioctl(fd, SIOCGIFINDEX, request)
            return request.getInt(IFNAMSIZ.toLong())
        }

        private fun nowMicros(): Long {
            val now = Instant.now()
            return now.epochSecond * 1_000_000L + now.nano / 1000
        }
    }
}
