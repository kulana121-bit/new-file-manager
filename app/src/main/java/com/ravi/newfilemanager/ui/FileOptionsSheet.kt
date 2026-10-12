package com.ravi.newfilemanager.ui

import android.content.Context
import android.content.Intent
import java.io.File

import androidx.compose.foundation.background
import androidx.compose.foundation.clickableimport androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.formatFileSize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Working "···" options sheet for a file: Open, Share, Details, Delete.
 */
@Composable
fun FileOptionsSheet(
    item: MediaItem,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onToast: (String) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    // scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(CardColor)
                .clickable(enabled = false) { }
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFD0D6DE))
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(40.dp))
                Text(
                    item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark,
                    maxLines = 1, modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LineColor)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            BottomSheetOption(
                icon = Icons.Default.OpenInNew, iconTint = BlueText, iconBg = BlueCard,
                title = "Open", subtitle = "Open this file",
                onClick = { onOpen(); onDismiss() }
            )
            BottomSheetOption(
                icon = Icons.Default.Share, iconTint = GreenText, iconBg = GreenCard,
                title = "Share", subtitle = "Share via other apps",
                onClick = { onShare(); onDismiss() }
            )
            BottomSheetOption(
                icon = Icons.Default.Info, iconTint = PurpleText, iconBg = PurpleCard,
                title = "Details", subtitle = "Size, date and location",
                onClick = { showDetails = true }
            )
            BottomSheetOption(
                icon = Icons.Default.DeleteForever, iconTint = Color(0xFFE85D5D), iconBg = Color(0xFFFFD4D4),
                title = "Delete", subtitle = "Permanently delete this file",
                onClick = { confirmDelete = true }
            )

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardColor)
                    .clickable { onDismiss() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
        }
    }

    if (showDetails) {
        FileDetailsDialog(item = item, onDismiss = { showDetails = false })
    }

    if (confirmDelete) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { confirmDelete = false },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .shadow(12.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardColor)
                    .clickable(enabled = false) { }
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Delete this file?", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))
                Text(item.name, fontSize = 13.sp, color = TextMuted, maxLines = 2)
                Spacer(modifier = Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LineColor)
                            .clickable { confirmDelete = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE5484D))
                            .clickable { confirmDelete = false; onDelete(); onDismiss() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FileDetailsDialog(item: MediaItem, onDismiss: () -> Unit) {
    val date = remember(item.date) {
        SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.US).format(Date(item.date))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(12.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(CardColor)
                .clickable(enabled = false) { }
                .padding(20.dp)
        ) {
            Text("File details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow("Name", item.name)
            DetailRow("Size", formatFileSize(item.size))
            DetailRow("Modified", date)
            DetailRow("Type", item.mime.ifBlank { "unknown" })
            DetailRow("Location", item.uri.toString().take(64))
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LineColor)
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Close", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(label, fontSize = 11.sp, color = TextMuted)
        Text(value, fontSize = 13.5.sp, color = TextDark, maxLines = 3)
    }
}

/** Share a media item via the system chooser. */
fun shareMediaItem(ctx: Context, item: MediaItem) {
    try {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = item.mime.ifBlank { "*/*" }
            putExtra(Intent.EXTRA_STREAM, item.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(send, "Share ${item.name}"))
    } catch (_: Exception) { }
}

/** Delete a media item (MediaStore or plain file). Returns true on success. */
fun deleteMediaItem(ctx: Context, item: MediaItem): Boolean {
    return try {
        if (item.uri.scheme == "content") {
            ctx.contentResolver.delete(item.uri, null, null) > 0
        } else {
            val p = item.uri.path ?: return false
            File(p).delete()
        }
    } catch (_: Exception) { false }
}
