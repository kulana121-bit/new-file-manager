package com.ravi.newfilemanager.data

import android.net.Uri

enum class Kind { IMAGE, VIDEO, AUDIO, DOC }

data class MediaItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val date: Long,
    val mime: String,
    val kind: Kind
)

fun kindFromMime(mime: String): Kind = when {
    mime.startsWith("image/") -> Kind.IMAGE
    mime.startsWith("video/") -> Kind.VIDEO
    mime.startsWith("audio/") -> Kind.AUDIO
    else -> Kind.DOC
}

fun kindFromName(name: String): Kind {
    val ext = name.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic" -> Kind.IMAGE
        "mp4", "mkv", "avi", "mov", "webm", "3gp" -> Kind.VIDEO
        "mp3", "wav", "m4a", "ogg", "flac", "aac" -> Kind.AUDIO
        else -> Kind.DOC
    }
}
