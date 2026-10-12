package com.ravi.newfilemanager.data

import java.io.File

data class FileItem(
    val file: File,
    val name: String = file.name,
    val path: String = file.absolutePath,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isDirectory) 0L else file.length(),
    val lastModified: Long = file.lastModified(),
    val extension: String = if (file.isDirectory) "" else file.name.substringAfterLast('.', "").lowercase()
) {
    val isImage: Boolean get() = extension in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    val isVideo: Boolean get() = extension in setOf("mp4", "mkv", "avi", "mov", "webm")
    val isAudio: Boolean get() = extension in setOf("mp3", "wav", "m4a", "ogg", "flac")
    val isDocument: Boolean get() = extension in setOf("pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx")
    val isArchive: Boolean get() = extension in setOf("zip", "rar", "7z", "tar", "gz")
    val isApk: Boolean get() = extension == "apk"
}

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.1f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.1f GB".format(gb)
}
