package com.ravi.newfilemanager.ui

import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem

/**
 * Simple in-memory navigation. All screens live in MainActivity;
 * complex payloads (file lists) travel in-process, no parceling needed.
 */
data class Route(
    val name: String,
    val title: String = "",
    val kind: Kind = Kind.IMAGE,
    val items: List<MediaItem> = emptyList(),
    val index: Int = 0,
    val item: MediaItem? = null,
    val category: String = ""
)
