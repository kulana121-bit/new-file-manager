package com.ravi.newfilemanager.data

import android.os.Environment
import android.os.StatFs
import java.io.File

object StorageStats {
    data class Info(val totalBytes: Long, val freeBytes: Long) {
        val usedBytes: Long get() = totalBytes - freeBytes
        val usedFraction: Float get() = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0f
    }

    fun primary(): Info = of(Environment.getExternalStorageDirectory())

    fun of(dir: File): Info {
        return try {
            val sf = StatFs(dir.absolutePath)
            Info(
                totalBytes = sf.blockCountLong * sf.blockSizeLong,
                freeBytes = sf.availableBlocksLong * sf.blockSizeLong
            )
        } catch (_: Exception) {
            Info(0L, 0L)
        }
    }
}
