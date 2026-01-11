package com.htb.gameesp

import java.io.File

object RootUtils {
    fun isDeviceRooted(): Boolean {
        val suPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/sbin/su",
            "/su/bin/su"
        )
        suPaths.forEach { if (File(it).exists()) return true }

        return executeRoot("id")?.contains("uid=0") == true
    }

    fun executeRoot(command: String): String? {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            process.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            null
        }
    }

    fun readMemoryInt(pid: Int, address: Long): Int {
        val bytes = readMemoryBytes(pid, address, 4) ?: return 0
        return ByteBuffer.wrap(bytes).int
    }

    fun readMemoryFloat(pid: Int, address: Long): Float {
        val bytes = readMemoryBytes(pid, address, 4) ?: return 0f
        return ByteBuffer.wrap(bytes).float
    }

    private fun readMemoryBytes(pid: Int, address: Long, size: Int): ByteArray? {
        return executeRoot("dd if=/proc/$pid/mem bs=1 skip=$address count=$size 2>/dev/null")?.toByteArray()
    }
}
