package com.ravi.newfilemanager.ui

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.MediaStoreRepo
import com.ravi.newfilemanager.data.StorageStats
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Map a media item to the right viewer route (null = no preview available). */
fun MediaItem.toRoute(): Route? = when (kind) {
    Kind.IMAGE -> Route("photo", items = listOf(this))
    Kind.VIDEO -> Route("video", item = this)
    Kind.AUDIO -> Route("audio", item = this)
    Kind.DOC -> {
        val n = name.lowercase()
        when {
            n.endsWith(".pdf") -> Route("pdf", item = this)
            n.endsWith(".html") || n.endsWith(".htm") -> Route("html", item = this)
            else -> null
        }
    }
}

fun mediaIcon(kind: Kind, name: String): ImageVector = when (kind) {
    Kind.IMAGE -> Icons.Default.Image
    Kind.VIDEO -> Icons.Default.Videocam
    Kind.AUDIO -> Icons.Default.MusicNote
    Kind.DOC -> when {
        name.endsWith(".pdf", true) -> Icons.Default.PictureAsPdf
        name.endsWith(".xls", true) || name.endsWith(".xlsx", true) -> Icons.Default.TableChart
        else -> Icons.Default.Description
    }
}

fun mediaTint(kind: Kind): androidx.compose.ui.graphics.Color = when (kind) {
    Kind.IMAGE -> PeachText
    Kind.VIDEO -> PurpleText
    Kind.AUDIO -> GreenText
    Kind.DOC -> BlueText
}

fun mediaBg(kind: Kind): androidx.compose.ui.graphics.Color = when (kind) {
    Kind.IMAGE -> PeachCard
    Kind.VIDEO -> PurpleCard
    Kind.AUDIO -> GreenCard
    Kind.DOC -> BlueCard
}

@Composable
fun DashboardScreen(
    go: (Route) -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    val stats = remember { StorageStats.primary() }
    var recent by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    LaunchedEffect(Unit) {
        recent = withContext(Dispatchers.IO) { MediaStoreRepo.recent(ctx, 8) }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Top bar: Search + Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SearchPill("Search files, folders...") { go(Route("search")) }
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(CardColor)
                        .clickable { onToast("No new notifications") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextDark, modifier = Modifier.size(20.dp))
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                // Storage card
                item {
                    SectionTitle("Storage")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardColor)
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = stats.usedFraction,
                                modifier = Modifier.size(72.dp),
                                color = Accent,
                                trackColor = LineColor,
                                strokeWidth = 8.dp
                            )
                            Text(formatFileSize(stats.usedBytes), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Storage Used", fontSize = 11.sp, color = TextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${formatFileSize(stats.usedBytes)} / ${formatFileSize(stats.totalBytes)}",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = stats.usedFraction,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Accent, trackColor = LineColor
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Free space: ${formatFileSize(stats.freeBytes)}", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                // Quick Access
                item {
                    SectionTitle("Quick Access")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickAccessCard("Images", PeachCard, PeachText, Modifier.weight(1f)) {
                                go(Route("category", title = "Images", kind = Kind.IMAGE))
                            }
                            QuickAccessCard("Videos", PurpleCard, PurpleText, Modifier.weight(1f)) {
                                go(Route("category", title = "Videos", kind = Kind.VIDEO))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            QuickAccessCard("Documents", BlueCard, BlueText, Modifier.weight(1f)) {
                                go(Route("category", title = "Documents", kind = Kind.DOC))
                            }
                            QuickAccessCard("Music", GreenCard, GreenText, Modifier.weight(1f)) {
                                go(Route("category", title = "Music", kind = Kind.AUDIO))
                            }
                        }
                    }
                }

                // Recent Files
                item { SectionTitle("Recent Files") }
                if (recent.isEmpty()) {
                    item {
                        Text("No recent files yet", fontSize = 13.sp, color = TextMuted,
                            modifier = Modifier.padding(vertical = 12.dp))
                    }
                } else {
                    items(recent) { item ->
                        val date = remember(item.date) {
                            SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(item.date))
                        }
                        FileRowItem(
                            iconBg = mediaBg(item.kind),
                            icon = mediaIcon(item.kind, item.name),
                            iconTint = mediaTint(item.kind),
                            title = item.name,
                            subtitle = "${formatFileSize(item.size)} · $date",
                            onClick = {
                                val r = item.toRoute()
                                if (r != null) go(r) else onToast("Preview not available for this file")
                            },
                            trailing = {
                                IconButton(onClick = {
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = item.mime
                                        putExtra(Intent.EXTRA_STREAM, item.uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    ctx.startActivity(Intent.createChooser(send, "Share"))
                                }, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextMuted, modifier = Modifier.size(17.dp))
                                }
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(86.dp)) }
            }
        }
        BottomDock(
            selected = "home",
            onHome = {},
            onBrowse = { go(Route("browse")) },
            onSettings = { go(Route("settings")) }
        )
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}
