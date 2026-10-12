package com.ravi.newfilemanager.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.VaultManager
import com.ravi.newfilemanager.data.formatFileSize
import com.ravi.newfilemanager.data.kindFromMime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GreenTint = Color(0xFFD4EDDA)
private val GreenIcon = Color(0xFF4CAF6D)

private data class VaultCat(val key: String, val title: String, val card: Color, val text: Color)

private val vaultCats = listOf(
    VaultCat("photos", "Private\nPhotos", PeachCard, PeachText),
    VaultCat("videos", "Secure\nVideo", PurpleCard, PurpleText),
    VaultCat("audio", "Private\nAudio", GreenCard, GreenText),
    VaultCat("docs", "Key\nDocuments", BlueCard, BlueText)
)

/** Decrypt a vault entry to a temp file and open it in the right viewer. */
fun openVaultEntry(
    scope: CoroutineScope,
    session: VaultManager.Session,
    entry: VaultManager.Entry,
    go: (Route) -> Unit,
    onToast: (String) -> Unit
) {
    scope.launch(Dispatchers.IO) {
        try {
            val tmp = session.decryptToTemp(entry)
            val media = MediaItem(
                uri = Uri.fromFile(tmp),
                name = entry.name,
                size = entry.size,
                date = entry.addedAt,
                mime = entry.mime,
                kind = kindFromMime(entry.mime)
            )
            withContext(Dispatchers.Main) {
                val r = media.toRoute()
                if (r != null) go(r) else onToast("Preview not available for this file")
            }
        } catch (_: Exception) {
            withContext(Dispatchers.Main) { onToast("Could not open file") }
        }
    }
}

@Composable
fun VaultUnlockedScreen(
    pin: String,
    onBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    go: (Route) -> Unit = {},
    onToast: (String) -> Unit = {}
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = remember { VaultManager(ctx) }
    val session = remember {
        try { vault.session(pin) } catch (_: Exception) { null }
    }

    var entries by remember { mutableStateOf<List<VaultManager.Entry>>(emptyList()) }
    fun reload() {
        scope.launch(Dispatchers.IO) {
            val all = listOf("photos", "videos", "audio", "docs").flatMap {
                try { vault.list(it) } catch (_: Exception) { emptyList() }
            }.sortedByDescending { it.addedAt }
            withContext(Dispatchers.Main) { entries = all }
        }
    }
    LaunchedEffect(Unit) { reload() }

    val totalSize = remember(entries) { entries.sumOf { it.size } }
    val countOf = remember(entries) { { cat: String -> entries.count { it.category == cat } } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextDark, modifier = Modifier.size(18.dp))
            }
            Text("Locked Files", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(CardColor)
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = TextDark, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Unlocked status card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardColor)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = "Unlocked", tint = GreenIcon, modifier = Modifier.size(22.dp))
                    }
                    Column {
                        Text("Unlocked", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("Secured Storage Unlocked", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Vault Highlights card (real counts)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(CardColor)
                        .padding(16.dp)
                ) {
                    Text("Vault Highlights", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total Protected: ${entries.size} Files (${formatFileSize(totalSize)})",
                        fontSize = 12.sp, color = TextDark
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Locked Categories (real counts)
            item {
                Text("Locked Categories", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    vaultCats.forEach { cat ->
                        VaultCategoryCard(
                            title = cat.title,
                            subtitle = "${countOf(cat.key)} files",
                            bgColor = cat.card,
                            textColor = cat.text,
                            onClick = { onCategoryClick(cat.key) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Locked Files header
            item {
                Text("Locked Files", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark,
                    modifier = Modifier.padding(bottom = 10.dp))
            }

            if (entries.isEmpty()) {
                item {
                    Text("No locked files yet — lock photos, videos or documents to see them here.",
                        fontSize = 13.sp, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    val kind = kindFromMime(entry.mime)
                    val date = remember(entry.addedAt) {
                        SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(entry.addedAt))
                    }
                    FileRowItem(
                        iconBg = mediaBg(kind),
                        icon = mediaIcon(kind, entry.name),
                        iconTint = mediaTint(kind),
                        title = entry.name,
                        subtitle = "${formatFileSize(entry.size)} · $date",
                        onClick = {
                            val s = session
                            if (s != null) openVaultEntry(scope, s, entry, go, onToast)
                            else onToast("Vault session expired")
                        },
                        trailing = {
                            Icon(Icons.Default.Lock, contentDescription = "Locked", tint = TextMuted,
                                modifier = Modifier.size(16.dp))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(90.dp)) }
        }
    }
}

@Composable
private fun VaultCategoryCard(
    title: String,
    subtitle: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(104.dp)
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor, lineHeight = 17.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 10.sp, color = textColor.copy(alpha = 0.75f))
    }
}
