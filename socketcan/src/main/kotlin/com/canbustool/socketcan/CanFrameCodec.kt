package com.canbustool.socketcan

import com.canbustool.core.CanMessage
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes/decodes Linux's `struct can_frame` (linux/can.h), written from the kernel's public
 * UAPI documentation (Documentation/networking/can.rst):
 * ```
 * struct can_frame {
 *     canid_t can_id;   // 32 bit: CAN_EFF_FLAG | CAN_RTR_FLAG | CAN_ERR_FLAG | id
 *     __u8    len;      // payload length, 0..8
 *     __u8    __pad, __res0, len8_dlc;
 *     __u8    data[8];
 * };
 * ```
 * The kernel uses host byte order for `can_id`.
 */
internal object CanFrameCodec {
    const val FRAME_SIZE = 16

    private const val EFF_FLAG = 0x80000000L
    private const val RTR_FLAG = 0x40000000L
    private const val EFF_MASK = 0x1FFFFFFFL
    private const val SFF_MASK = 0x7FFL

    fun encode(id: Long, extended: Boolean, remote: Boolean, data: ByteArray): ByteArray {
        require(data.size <= 8) { "Classic CAN frames carry at most 8 data bytes" }
        var canId = if (extended) (id and EFF_MASK) or EFF_FLAG else id and SFF_MASK
        if (remote) canId = canId or RTR_FLAG
        val buffer = ByteBuffer.allocate(FRAME_SIZE).order(ByteOrder.nativeOrder())
        buffer.putInt(canId.toInt())
        buffer.put(data.size.toByte())
        buffer.position(8)
        buffer.put(data)
        return buffer.array()
    }

    /** Returns null for error frames (`CAN_ERR_FLAG`), which carry bus diagnostics rather than data. */
    fun decode(frame: ByteArray, timestampMicros: Long): CanMessage? {
        val buffer = ByteBuffer.wrap(frame).order(ByteOrder.nativeOrder())
        val canId = buffer.getInt(0).toLong() and 0xFFFFFFFFL
        if (canId and ERR_FLAG != 0L) return null
        val extended = canId and EFF_FLAG != 0L
        val length = (frame[4].toInt() and 0xFF).coerceIn(0, 8)
        return CanMessage(
            id = canId and if (extended) EFF_MASK else SFF_MASK,
            extended = extended,
            remote = canId and RTR_FLAG != 0L,
            data = frame.copyOfRange(8, 8 + length),
            timestampMicros = timestampMicros,
        )
    }

    private const val ERR_FLAG = 0x20000000L
}
