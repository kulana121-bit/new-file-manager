package com.ravi.newfilemanager.ui

import android.content.Intent
import android.media.MediaPlayer
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
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
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AudioPlayerScreen(
    item: MediaItem,
    onBack: () -> Unit,
    onToast: (String) -> Unit,
    onLockRequest: (MediaItem) -> Unit
) {
    val ctx = LocalContext.current
    val player = remember {
        MediaPlayer().apply {
            try {
                setDataSource(ctx, item.uri)
                prepareAsync()
            } catch (_: Exception) { }
        }
    }
    var playing by remember { mutableStateOf(false) }
    var pos by remember { mutableIntStateOf(0) }
    var dur by remember { mutableIntStateOf(1) }
    var confirmDelete by remember { mutableStateOf(false) }

    DisposableEffect(item.uri) {
        player.setOnPreparedListener { mp ->
            dur = mp.duration.coerceAtLeast(1)
            mp.start(); playing = true
        }
        player.setOnCompletionListener { playing = false; pos = 0 }
        player.setOnErrorListener { _, _, _ -> onToast("Cannot play this file"); true }
        onDispose {
            try { if (player.isPlaying) player.stop() } catch (_: Exception) { }
            player.release()
        }
    }

    LaunchedEffect(playing) {
        while (playing) {
            delay(500)
            try {
                if (player.isPlaying) {
                    pos = player.currentPosition
                    if (dur <= 1) dur = player.duration.coerceAtLeast(1)
                }
            } catch (_: Exception) { }
        }
    }

    val date = remember(item.date) {
        SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(item.date))
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            ScreenHeader(title = "", onBack = onBack,
                actionIcon = Icons.Default.MoreHoriz, onAction = { onToast("Details in next update") })

            // album art
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 34.dp)
                    .size(190.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFF6B9D), Color(0xFFFFA26B), Color(0xFF6BD5FF), Color(0xFF9D6BFF))
                        )
                    )
            )

            Spacer(modifier = Modifier.height(22.dp))
            Text(item.name, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = TextDark,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            Spacer(modifier = Modifier.height(6.dp))
            Text("${formatFileSize(item.size)} · $date", fontSize = 12.5.sp, color = TextMuted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

            // transport
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(26.dp, Alignment.CenterHorizontally)
            ) {
                SkipButton("-10s") {
                    try { player.seekTo((player.currentPosition - 10000).coerceAtLeast(0)) } catch (_: Exception) { }
                }
                IconButton(
                    onClick = {
                        try {
                            if (player.isPlaying) { player.pause(); playing = false }
                            else { player.start(); playing = true }
                        } catch (_: Exception) { }
                    },
                    modifier = Modifier.size(84.dp).clip(CircleShape).background(CardColor)
                ) {
                    Icon(
                        if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null, tint = TextDark, modifier = Modifier.size(38.dp)
                    )
                }
                SkipButton("+10s") {
                    try { player.seekTo((player.currentPosition + 10000).coerceAtMost(dur)) } catch (_: Exception) { }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Slider(
                value = pos.toFloat(),
                onValueChange = {
                    pos = it.toInt()
                    try { player.seekTo(pos) } catch (_: Exception) { }
                },
                valueRange = 0f..dur.toFloat(),
                colors = SliderDefaults.colors(thumbColor = Accent, activeTrackColor = Accent)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fmtTime(pos), fontSize = 11.5.sp, color = TextMuted)
                Text("-" + fmtTime(dur - pos), fontSize = 11.5.sp, color = TextMuted)
            }

            Spacer(modifier = Modifier.weight(1f))

            // glass action bar
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardColor)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewerAction(Icons.Default.Lock, "Lock") { onLockRequest(item) }
                ViewerAction(Icons.Default.PlaylistAdd, "Playlist") { onToast("Playlists in next update") }
                ViewerAction(Icons.Default.Share, "Share") {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = item.mime
                        putExtra(Intent.EXTRA_STREAM, item.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    ctx.startActivity(Intent.createChooser(send, "Share"))
                }
                ViewerAction(Icons.Default.Delete, "Delete", tintRed = true) { confirmDelete = true }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete this audio?") },
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
