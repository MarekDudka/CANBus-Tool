package com.canbustool.socketcan

import com.sun.jna.Library
import com.sun.jna.LastErrorException
import com.sun.jna.Native
import com.sun.jna.Pointer

/**
 * The few libc calls needed to talk to a SocketCAN interface. This is the operating system's own
 * C library, not a vendor library — nothing extra needs to be installed. Methods that report
 * failure via `-1`/errno throw [LastErrorException], carrying the errno.
 */
internal interface LibC : Library {
    @Throws(LastErrorException::class)
    fun socket(domain: Int, type: Int, protocol: Int): Int

    @Throws(LastErrorException::class)
    fun bind(fd: Int, address: Pointer, length: Int): Int

    @Throws(LastErrorException::class)
    fun ioctl(fd: Int, request: Long, argument: Pointer): Int

    @Throws(LastErrorException::class)
    fun read(fd: Int, buffer: Pointer, count: Long): Long

    @Throws(LastErrorException::class)
    fun write(fd: Int, buffer: Pointer, count: Long): Long

    @Throws(LastErrorException::class)
    fun close(fd: Int): Int

    companion object {
        val INSTANCE: LibC by lazy { Native.load("c", LibC::class.java) }
    }
}
