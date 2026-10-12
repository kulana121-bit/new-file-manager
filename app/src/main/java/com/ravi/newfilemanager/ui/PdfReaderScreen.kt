package com.ravi.newfilemanager.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfReaderScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onLockRequest: (MediaItem) -> Unit
) {
    val ctx = LocalContext.current
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageCount by remember { mutableIntStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(item.uri) {
        withContext(Dispatchers.IO) {
            try {
                val fd = ctx.contentResolver.openFileDescriptor(item.uri, "r")
                pfd = fd
                if (fd != null) {
                    val r = PdfRenderer(fd)
                    renderer = r
                    pageCount = r.pageCount
                } else failed = true
            } catch (_: Exception) { failed = true }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try { renderer?.close() } catch (_: Exception) { }
            try { pfd?.close() } catch (_: Exception) { }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.padding(horizontal = 20.dp)) {
                ScreenHeader(title = "", onBack = onBack)
            }
            Text(
                item.name, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = TextDark,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                textAlign = TextAlign.Center, maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pageCount > 0) {
                Box(modifier = Modifier.align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(13.dp)).background(CardColor)
                    .padding(horizontal = 15.dp, vertical = 7.dp)) {
                    Text("Page 1 of $pageCount", fontSize = 12.sp, color = TextMuted)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp)) {
                when {
                    failed -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cannot open this PDF", fontSize = 14.sp, color = TextMuted)
                    }
                    pageCount == 0 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(pageCount) { page ->
                            val bmp = remember(page, renderer) {
                                try {
                                    renderer?.let { r ->
                                        r.openPage(page).use { p ->
                                            val scale = 2f
                                            val b = Bitmap.createBitmap(
                                                (p.width * scale).toInt(),
                                                (p.height * scale).toInt(),
                                                Bitmap.Config.ARGB_8888
                                            )
                                            b.eraseColor(android.graphics.Color.WHITE)
                                            p.render(b, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                            b
                                        }
                                    }
                                } catch (_: Exception) { null }
                            }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Page ${page + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(6.dp, RoundedCornerShape(12.dp))
                                        .clip(RoundedCornerShape(12.dp))
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }

            // labeled glass action bar
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardColor)
                    .padding(horizontal = 6.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabeledAction(Icons.Default.Lock, "Lock /\nUnhide", TextDark) { onLockRequest(item) }
                LabeledAction(Icons.Default.Search, "Search /\nFind", TextDark) { onToast("Find in document in next update") }
                LabeledAction(Icons.Default.Print, "Print /\nExport", TextDark) { onToast("Print in next update") }
                LabeledAction(Icons.Default.Share, "Share /\nExport", TextDark) {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = item.mime
                        putExtra(Intent.EXTRA_STREAM, item.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share"))
                }
                LabeledAction(Icons.Default.Delete, "Delete", Color(0xFFE5484D)) { confirmDelete = true }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete this file?") },
                text = { Text(item.name) },
                confirmButton = {
                    TextButton(onClick = {
                        try { ctx.contentResolver.delete(item.uri, null, null); onToast("Deleted") }
                        catch (_: Exception) { onToast("Could not delete") }
                        confirmDelete = false; onBack()
                    }) { Text("Delete", color = Color(0xFFE5484D)) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
            )
        }
    }
}

@Composable
fun LabeledAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    androidx.compose.material3.IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.material3.Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(label, fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = tint,
                textAlign = TextAlign.Center, lineHeight = 10.sp)
        }
    }
}

