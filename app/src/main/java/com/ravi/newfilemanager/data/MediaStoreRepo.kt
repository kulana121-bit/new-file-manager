package com.ravi.newfilemanager.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

object MediaStoreRepo {

    private val IMG_PROJ = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DATE_MODIFIED,
        MediaStore.Images.Media.MIME_TYPE
    )
    private val VID_PROJ = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.MIME_TYPE
    )
    private val AUD_PROJ = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_MODIFIED,
        MediaStore.Audio.Media.MIME_TYPE
    )
    private val FILE_PROJ = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.DISPLAY_NAME,
        MediaStore.Files.FileColumns.SIZE,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
        MediaStore.Files.FileColumns.MIME_TYPE
    )

    private const val SORT = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

    fun images(ctx: Context, limit: Int = 200): List<MediaItem> =
        query(ctx, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMG_PROJ, null, Kind.IMAGE, limit)

    fun videos(ctx: Context, limit: Int = 200): List<MediaItem> =
        query(ctx, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VID_PROJ, null, Kind.VIDEO, limit)

    fun audio(ctx: Context, limit: Int = 200): List<MediaItem> =
        query(ctx, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUD_PROJ, null, Kind.AUDIO, limit)

    fun documents(ctx: Context, limit: Int = 200): List<MediaItem> {
        val sel = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR " +
                "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ?"
        val args = arrayOf("application/pdf", "application/msword%", "application/vnd.openxmlformats%", "text/%")
        return query(ctx, MediaStore.Files.getContentUri("external"), FILE_PROJ, sel to args, Kind.DOC, limit)
    }

    fun recent(ctx: Context, limit: Int = 12): List<MediaItem> {
        val all = images(ctx, 60) + videos(ctx, 60) + audio(ctx, 40) + documents(ctx, 40)
        return all.sortedByDescending { it.date }.take(limit)
    }

    fun search(ctx: Context, q: String): List<MediaItem> {
        if (q.isBlank()) return emptyList()
        val like = "%$q%"
        val sel = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val imgs = query(ctx, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMG_PROJ, sel to arrayOf(like), Kind.IMAGE, 50)
        val vids = query(ctx, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VID_PROJ, sel to arrayOf(like), Kind.VIDEO, 50)
        val auds = query(ctx, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, AUD_PROJ, sel to arrayOf(like), Kind.AUDIO, 50)
        val docs = query(ctx, MediaStore.Files.getContentUri("external"), FILE_PROJ,
            "(${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'application/%' OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'text/%') AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?" to arrayOf(like),
            Kind.DOC, 50)
        return (imgs + vids + auds + docs).sortedByDescending { it.date }
    }

    private fun query(
        ctx: Context,
        uri: Uri,
        proj: Array<String>,
        sel: Pair<String, Array<String>>?,
        kind: Kind,
        limit: Int
    ): List<MediaItem> {
        val out = ArrayList<MediaItem>()
        try {
            val cr: ContentResolver = ctx.contentResolver
            // LIMIT via bundle only on API 30+; otherwise take() below
            val cursor = if (Build.VERSION.SDK_INT >= 30) {
                val b = android.os.Bundle().apply {
                    putString(ContentResolver.QUERY_ARG_SQL_SELECTION, sel?.first)
                    putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, sel?.second)
                    putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, SORT)
                    putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
                }
                cr.query(uri, proj, b, null)
            } else {
                cr.query(uri, proj, sel?.first, sel?.second, SORT)
            } ?: return out
            cursor.use {
                val iId = it.getColumnIndexOrThrow(proj[0])
                val iName = it.getColumnIndexOrThrow(proj[1])
                val iSize = it.getColumnIndexOrThrow(proj[2])
                val iDate = it.getColumnIndexOrThrow(proj[3])
                val iMime = it.getColumnIndexOrThrow(proj[4])
                while (it.moveToNext() && out.size < limit) {
                    val id = it.getLong(iId)
                    val mime = it.getString(iMime) ?: "application/octet-stream"
                    out.add(
                        MediaItem(
                            uri = ContentUris.withAppendedId(uri, id),
                            name = it.getString(iName) ?: "unknown",
                            size = it.getLong(iSize),
                            date = it.getLong(iDate) * 1000L,
                            mime = mime,
                            kind = if (kind == Kind.DOC) kindFromMime(mime) else kind
                        )
                    )
                }
            }
        } catch (_: Exception) { }
        return out
    }
}
