package com.ravi.newfilemanager.ui

import android.content.Intent
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ravi.newfilemanager.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HtmlViewerScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onLockRequest: (MediaItem) -> Unit
) {
    val ctx = LocalContext.current
    var tab by remember { mutableIntStateOf(1) } // 0 = Preview, 1 = Source Code
    var source by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(item.uri) {
        source = withContext(Dispatchers.IO) {
            try {
                ctx.contentResolver.openInputStream(item.uri)?.use { ins ->
                    val bytes = ins.readBytes().take(200_000).toByteArray()
                    bytes.toString(Charsets.UTF_8)
                }
            } catch (_: Exception) { null }
        }
    }

    val darkBg = Color(0xFF131A26)

    Box(modifier = Modifier.fillMaxSize().background(BgColor).padding(14.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(26.dp))
                .background(darkBg)
                .padding(14.dp)
        ) {
            ScreenHeaderDark(onBack = onBack, badge = "✏️ Source", onMore = { onToast("Details in next update") })

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                item.name, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 1
            )
            Spacer(modifier = Modifier.height(10.dp))

            // segmented tabs
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(4.dp)
            ) {
                SegTab("Preview", tab == 0) {
                    tab = 0
                    if (source == null) onToast("Preview renders in build")
                }
                SegTab("Source Code", tab == 1) { tab = 1 }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    source == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                    tab == 0 -> AndroidView(
                        factory = { c ->
                            WebView(c).apply {
                                settings.javaScriptEnabled = false
                                loadDataWithBaseURL(null, source ?: "", "text/html", "utf-8", null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                    )
                    else -> {
                        val numbered = remember(source) {
                            source!!.lines().mapIndexed { i, l ->
                                "%3d  %s".format(i + 1, l)
                            }.joinToString("\n")
                        }
                        Text(
                            numbered,
                            fontSize = 11.sp, lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFDBE4F3),
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // labeled glass action bar (dark)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(horizontal = 6.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabeledAction(Icons.Default.Lock, "Lock /\nUnhide", Color.White) { onLockRequest(item) }
                LabeledAction(Icons.Default.Edit, "Edit /\nRewrite", Color.White) { onToast("Editor in next update") }
                LabeledAction(Icons.Default.Search, "Find &\nReplace", Color.White) { onToast("Find & replace in next update") }
                LabeledAction(Icons.Default.Share, "Share /\nExport", Color.White) {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = item.mime
                        putExtra(Intent.EXTRA_STREAM, item.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share"))
                }
                LabeledAction(Icons.Default.Delete, "Delete", Color(0xFFFF7A7A)) { confirmDelete = true }
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
private fun SegTab(label: String, on: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (on) Color(0x38FFFFFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold,
            color = if (on) Color.White else Color(0xFF9FB0C8))
    }
}

@Composable
private fun ScreenHeaderDark(
    onBack: () -> Unit,
    badge: String,
    onMore: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x1FFFFFFF))
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(badge, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x73000000))
                    .padding(horizontal = 16.dp, vertical = 9.dp))
        }
        IconButton(
            onClick = onMore,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0x1FFFFFFF))
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
