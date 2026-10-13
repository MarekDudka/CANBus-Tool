package com.canbustool.socketcan

import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CanFrameCodecTest {
    private fun canId(frame: ByteArray) = ByteBuffer.wrap(frame).order(ByteOrder.nativeOrder()).getInt(0).toLong() and 0xFFFFFFFFL

    @Test
    fun standardFrameRoundTrips() {
        val frame = CanFrameCodec.encode(0x123, extended = false, remote = false, data = byteArrayOf(1, 2, 3))
        assertEquals(16, frame.size)
        assertEquals(0x123L, canId(frame))
        assertEquals(3, frame[4].toInt())
        val decoded = CanFrameCodec.decode(frame, 42)!!
        assertEquals(0x123L, decoded.id)
        assertEquals(false, decoded.extended)
        assertContentEquals(byteArrayOf(1, 2, 3), decoded.data)
        assertEquals(42L, decoded.timestampMicros)
    }

    @Test
    fun extendedRemoteFrameSetsFlags() {
        val frame = CanFrameCodec.encode(0x1ABCDEF0, extended = true, remote = true, data = byteArrayOf())
        assertEquals(0x1ABCDEF0L or 0x80000000L or 0x40000000L, canId(frame))
        val decoded = CanFrameCodec.decode(frame, 0)!!
        assertEquals(0x1ABCDEF0L, decoded.id)
        assertTrue(decoded.extended)
        assertTrue(decoded.remote)
    }

    @Test
    fun errorFramesAreDropped() {
        val frame = ByteArray(16)
        ByteBuffer.wrap(frame).order(ByteOrder.nativeOrder()).putInt(0, 0x20000004)
        assertNull(CanFrameCodec.decode(frame, 0))
    }
}
