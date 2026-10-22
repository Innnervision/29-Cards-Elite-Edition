package com.example.game.crash

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.zip.CRC32

object SafeDataManager {

    private const val TAG = "SafeDataManager"

    fun writeTextSafely(targetFile: File, content: String): Boolean {
        return try {
            val parent = targetFile.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            val tempFile = File(targetFile.parent, "${targetFile.name}.tmp")
            val backupFile = File(targetFile.parent, "${targetFile.name}.bak")

            // Write to temporary file first
            FileOutputStream(tempFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
                fos.fd.sync()
            }

            // If target file already exists, promote to backup
            if (targetFile.exists()) {
                if (backupFile.exists()) backupFile.delete()
                targetFile.renameTo(backupFile)
            }

            // Atomic rename temp to target
            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                // Fallback direct copy if rename failed
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Safe write failed for ${targetFile.name}: ${e.message}", e)
            false
        }
    }

    fun readTextSafely(targetFile: File): String? {
        val backupFile = File(targetFile.parent, "${targetFile.name}.bak")

        // 1. Try reading primary target file
        if (targetFile.exists()) {
            try {
                val content = targetFile.readText(Charsets.UTF_8)
                if (content.isNotBlank()) return content
            } catch (e: Exception) {
                Log.w(TAG, "Primary file ${targetFile.name} read error: ${e.message}. Attempting backup recovery...")
            }
        }

        // 2. Fallback to backup file
        if (backupFile.exists()) {
            try {
                val content = backupFile.readText(Charsets.UTF_8)
                Log.i(TAG, "Successfully restored data from backup file ${backupFile.name}")
                // Restore primary from backup
                backupFile.copyTo(targetFile, overwrite = true)
                return content
            } catch (e: Exception) {
                Log.e(TAG, "Backup file read error: ${e.message}", e)
            }
        }

        return null
    }

    fun calculateChecksum(data: String): Long {
        val crc = CRC32()
        crc.update(data.toByteArray(Charsets.UTF_8))
        return crc.value
    }
}
