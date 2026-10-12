package com.ravi.newfilemanager.ui

import android.content.Intent
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.formatFileSize

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoViewerScreen(
    items: List<MediaItem>,
    startIndex: Int,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onLockRequest: (MediaItem) -> Unit
) {
    val ctx = LocalContext.current
    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(items.indices),
        pageCount = { items.size }
    )
    var starred by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val current = items.getOrNull(pagerState.currentPage)

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val item = items[page]
            AndroidView(
                factory = { c ->
                    ImageView(c).apply {
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        adjustViewBounds = true
                    }
                },
                update = { it.setImageURI(item.uri) },
                modifier = Modifier.fillMaxSize().padding(bottom = 120.dp, top = 70.dp)
            )
        }

        // top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CardColor)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(18.dp))
            }
            Text(
                current?.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center, maxLines = 1
            )
            IconButton(
                onClick = { onToast("Details in next update") },
                modifier = Modifier.size(40.dp).clip(CircleShape).background(CardColor)
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = TextDark, modifier = Modifier.size(18.dp))
            }
        }
        current?.let {
            Text(
                "${formatFileSize(it.size)}",
                fontSize = 11.5.sp, color = TextMuted,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 66.dp)
            )
        }

        // bottom glass action bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 26.dp, start = 24.dp, end = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CardColor)
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewerAction(Icons.Default.Lock, "Lock") { current?.let(onLockRequest) }
            ViewerAction(if (starred) Icons.Default.Star else Icons.Default.StarBorder, "Favorite") { starred = !starred }
            ViewerAction(Icons.Default.Share, "Share") {
                current?.let {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = it.mime
                        putExtra(Intent.EXTRA_STREAM, it.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share"))
                }
            }
            ViewerAction(Icons.Default.Delete, "Delete", tintRed = true) { confirmDelete = true }
        }

        if (confirmDelete && current != null) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete this photo?") },
                text = { Text(current.name) },
                confirmButton = {
                    TextButton(onClick = {
                        try {
                            ctx.contentResolver.delete(current.uri, null, null)
                            onToast("Deleted")
                        } catch (_: Exception) { onToast("Could not delete") }
                        confirmDelete = false
                        onBack()
                    }) { Text("Delete", color = androidx.compose.ui.graphics.Color(0xFFE5484D)) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
            )
        }
        Spacer(modifier = Modifier.size(0.dp))
    }
}

@Composable
fun ViewerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    tintRed: Boolean = false,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(46.dp)) {
        Icon(
            icon, contentDescription = desc,
            tint = if (tintRed) androidx.compose.ui.graphics.Color(0xFFE5484D) else TextDark,
            modifier = Modifier.size(22.dp)
        )
    }
}
