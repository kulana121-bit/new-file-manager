package com.ravi.newfilemanager.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.VaultManager
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PrivatePhotosScreen(
    pin: String,
    onBack: () -> Unit,
    go: (Route) -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = remember { VaultManager(ctx) }
    val session = remember {
        try { vault.session(pin) } catch (_: Exception) { null }
    }

    var entries by remember { mutableStateOf<List<VaultManager.Entry>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch(Dispatchers.IO) {
            val list = try { vault.list("photos") } catch (_: Exception) { emptyList() }
            withContext(Dispatchers.Main) { entries = list }
        }
    }
    LaunchedEffect(Unit) { reload() }

    val filtered = remember(entries, query) {
        if (query.isBlank()) entries
        else entries.filter { it.name.contains(query, ignoreCase = true) }
    }
    val totalSize = remember(entries) { entries.sumOf { it.size } }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgColor)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            ScreenHeader(
                title = "Private Photos",
                onBack = onBack,
                actionIcon = Icons.Default.Sort,
                onAction = { onToast("Sort options in next update") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted,
                    modifier = Modifier.size(18.dp).padding(start = 8.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search in Private Photos...", fontSize = 13.sp, color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Category Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                "${entries.size} Photos · ${formatFileSize(totalSize)}",
                fontSize = 12.sp, color = TextMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            if (session == null) {
                Text("Could not unlock vault", fontSize = 14.sp, color = Color(0xFFE5484D))
            } else if (filtered.isEmpty() && query.isBlank()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No locked photos yet", fontSize = 14.sp, color = TextMuted)
                        Text("Tap Add Photo to lock your first one", fontSize = 12.sp, color = TextMuted)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { entry ->
                        VaultPhotoItem(
                            entry = entry,
                            session = session,
                            selecting = selecting,
                            isSelected = entry.id in selected,
                            onClick = {
                                if (selecting) {
                                    selected = if (entry.id in selected) selected - entry.id else selected + entry.id
                                } else {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            val tmp = session.decryptToTemp(entry)
                                            val media = MediaItem(
                                                uri = Uri.fromFile(tmp),
                                                name = entry.name,
                                                size = entry.size,
                                                date = entry.addedAt,
                                                mime = entry.mime,
                                                kind = Kind.IMAGE
                                            )
                                            withContext(Dispatchers.Main) {
                                                go(Route("photo", items = listOf(media)))
                                            }
                                        } catch (_: Exception) {
                                            withContext(Dispatchers.Main) { onToast("Could not open photo") }
                                        }
                                    }
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        if (!selecting) {
            // Bottom action buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardColor)
                        .clickable { showAddSheet = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add Photo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardColor)
                        .clickable { selecting = true }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bulk Options", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                }
            }
        } else {
            // selection action bar
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardColor)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    if (selected.isEmpty()) "Select photos" else "${selected.size} selected",
                    fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark,
                    modifier = Modifier.clickable {
                        selecting = false; selected = emptySet()
                    }
                )
                Text(
                    "Delete",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = if (selected.isEmpty()) TextMuted else Color(0xFFE5484D),
                    modifier = Modifier.clickable(enabled = selected.isNotEmpty()) { showDeleteSheet = true }
                )
            }
        }
    }

    if (showAddSheet) {
        AddPhotoBottomSheet(
            pin = pin,
            category = "photos",
            onDismiss = { showAddSheet = false },
            onLocked = { reload() },
            onToast = onToast
        )
    }
    if (showDeleteSheet) {
        DeletePhotoBottomSheet(
            count = selected.size,
            onDismiss = { showDeleteSheet = false },
            onRemoveFromDevice = {
                scope.launch(Dispatchers.IO) {
                    entries.filter { it.id in selected }.forEach {
                        try { vault.delete(it) } catch (_: Exception) { }
                    }
                    withContext(Dispatchers.Main) {
                        selected = emptySet(); selecting = false
                        reload(); onToast("Removed from vault")
                    }
                }
            },
            onDeleteFromBoth = {
                scope.launch(Dispatchers.IO) {
                    entries.filter { it.id in selected }.forEach {
                        try { vault.delete(it) } catch (_: Exception) { }
                    }
                    withContext(Dispatchers.Main) {
                        selected = emptySet(); selecting = false
                        reload(); onToast("Permanently deleted")
                    }
                }
            },
            onToast = onToast
        )
    }
}

@Composable
private fun VaultPhotoItem(
    entry: VaultManager.Entry,
    session: VaultManager.Session,
    selecting: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var bmp by remember(entry.id) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(entry.id) {
        bmp = withContext(Dispatchers.IO) {
            try {
                val bytes = session.decrypt(entry)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (_: Exception) { null }
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.78f)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(PeachCard)
                .clickable { onClick() }
                .then(
                    if (isSelected) Modifier.border(3.dp, Accent, RoundedCornerShape(16.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (bmp != null) {
                Image(
                    bitmap = bmp!!.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(Icons.Default.Image, contentDescription = null, tint = PeachText, modifier = Modifier.size(30.dp))
            }
            if (selecting) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Accent else Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(entry.name, fontSize = 10.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatFileSize(entry.size), fontSize = 9.sp, color = TextMuted)
    }
}
