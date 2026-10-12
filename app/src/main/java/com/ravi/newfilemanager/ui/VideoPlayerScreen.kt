package com.ravi.newfilemanager.ui

import android.media.MediaPlayer
import android.widget.VideoView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ravi.newfilemanager.data.MediaItem
import kotlinx.coroutines.delay

fun fmtTime(ms: Int): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}

@Composable
fun VideoPlayerScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onLockRequest: (MediaItem) -> Unit
) {
    val ctx = LocalContext.current
    var videoView by remember { mutableStateOf<VideoView?>(null) }
    var mpRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var pos by remember { mutableIntStateOf(0) }
    var dur by remember { mutableIntStateOf(1) }
    var confirmDelete by remember { mutableStateOf(false) }

    DisposableEffect(item.uri) {
        onDispose {
            try { videoView?.stopPlayback() } catch (_: Exception) { }
        }
    }

    LaunchedEffect(playing) {
        while (playing) {
            delay(500)
            videoView?.let {
                if (it.isPlaying) {
                    pos = it.currentPosition
                    if (dur <= 1) dur = it.duration.coerceAtLeast(1)
                } else if (pos >= dur - 500) {
                    playing = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF5B7FA8), Color(0xFF2C4A66))))
    ) {
        AndroidView(
            factory = { c ->
                VideoView(c).apply {
                    setVideoURI(item.uri)
                    setOnPreparedListener { mp ->
                        mpRef = mp
                        dur = mp.duration.coerceAtLeast(1)
                        start(); playing = true
                    }
                    setOnCompletionListener { playing = false; pos = 0 }
                    videoView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // top bar
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0x66000000))) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White,
                modifier = Modifier.weight(1f), textAlign = TextAlign.Center, maxLines = 1
            )
            IconButton(onClick = { onToast("Details in next update") }, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0x66000000))) {
                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // center controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            SkipButton("-10s") { videoView?.let { it.seekTo((it.currentPosition - 10000).coerceAtLeast(0)) } }
            IconButton(
                onClick = {
                    videoView?.let { vv ->
                        if (vv.isPlaying) { vv.pause(); playing = false }
                        else { vv.start(); playing = true }
                    }
                },
                modifier = Modifier.size(84.dp).clip(CircleShape).background(Color(0x55FFFFFF))
            ) {
                Icon(
                    if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp)
                )
            }
            SkipButton("+10s") { videoView?.let { it.seekTo((it.currentPosition + 10000).coerceAtMost(dur)) } }
        }

        // seek + actions
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 18.dp)) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Slider(
                    value = pos.toFloat(),
                    onValueChange = { pos = it.toInt(); videoView?.seekTo(pos) },
                    valueRange = 0f..dur.toFloat(),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fmtTime(pos), fontSize = 11.sp, color = Color.White)
                    Text("-" + fmtTime(dur - pos), fontSize = 11.sp, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0x88FFFFFF))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewerActionWhite(Icons.Default.Lock, "Lock") { onLockRequest(item) }
                ViewerActionWhite(if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, "Mute") {
                    muted = !muted
                    mpRef?.setVolume(if (muted) 0f else 1f, if (muted) 0f else 1f)
                }
                ViewerActionWhite(Icons.Default.Share, "Share") {
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = item.mime
                        putExtra(android.content.Intent.EXTRA_STREAM, item.uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ctx.startActivity(android.content.Intent.createChooser(send, "Share"))
                }
                ViewerActionWhite(Icons.Default.Delete, "Delete", red = true) { confirmDelete = true }
            }
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete this video?") },
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
fun SkipButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0x44FFFFFF))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun ViewerActionWhite(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, red: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(icon, contentDescription = desc, tint = if (red) Color(0xFFE5484D) else Color.White, modifier = Modifier.size(22.dp))
    }
}
