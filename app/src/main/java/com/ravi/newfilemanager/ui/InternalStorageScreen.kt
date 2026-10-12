package com.ravi.newfilemanager.ui

import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.Prefs
import com.ravi.newfilemanager.data.StorageStats
import com.ravi.newfilemanager.data.formatFileSize
import com.ravi.newfilemanager.data.kindFromMime
import com.ravi.newfilemanager.data.kindFromName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class RootFolder(val label: String, val type: String)

private val ROOTS = listOf(
    RootFolder("Documents", Environment.DIRECTORY_DOCUMENTS),
    RootFolder("Download", Environment.DIRECTORY_DOWNLOADS),
    RootFolder("DCIM (Camera)", Environment.DIRECTORY_DCIM),
    RootFolder("Movies", Environment.DIRECTORY_MOVIES),
    RootFolder("Music", Environment.DIRECTORY_MUSIC)
)

@Composable
fun InternalStorageScreen(
    go: (Route) -> Unit,
    onBack: () -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var storage by remember { mutableStateOf(StorageStats.Info(0, 0)) }
    var stack by remember { mutableStateOf<List<DocumentFile>>(emptyList()) }
    var children by remember { mutableStateOf<List<DocumentFile>?>(null) }
    var grantFor by remember { mutableStateOf<RootFolder?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }

    val treeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        val target = grantFor
        if (uri != null && target != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                prefs.setSafUri(target.label, uri.toString())
                refreshTick++
                onToast("Access granted: ${target.label}")
            } catch (_: Exception) {
                onToast("Could not keep access permission")
            }
        }
        grantFor = null
    }

    fun rootDoc(label: String): DocumentFile? {
        val saved = prefs.safUri(label) ?: return null
        return try {
            val uri = Uri.parse(saved)
            // verify we still hold permission
            val held = ctx.contentResolver.persistedUriPermissions.any { it.uri == uri }
            if (!held) null else DocumentFile.fromTreeUri(ctx, uri)
        } catch (_: Exception) { null }
    }

    LaunchedEffect(Unit) {
        storage = withContext(Dispatchers.IO) { StorageStats.primary() }
    }

    // load children of current dir
    LaunchedEffect(stack, refreshTick) {
        children = withContext(Dispatchers.IO) {
            val dir = stack.lastOrNull() ?: return@withContext null
            try {
                dir.listFiles().sortedWith(
                    compareBy({ !it.isDirectory }, { it.name?.lowercase() })
                )
            } catch (_: Exception) { emptyList() }
        }
    }

    val currentTitle = stack.lastOrNull()?.name ?: "Internal Storage"

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ScreenHeader(
            title = currentTitle,
            onBack = {
                if (stack.isNotEmpty()) stack = stack.dropLast(1) else onBack()
            },
            actionIcon = Icons.Default.GridView,
            onAction = { onToast("Grid view in next update") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (stack.isEmpty()) {
            // storage summary
            Text("${formatFileSize(storage.freeBytes)} free of ${formatFileSize(storage.totalBytes)}",
                fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = storage.usedFraction,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Accent, trackColor = LineColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("Tap a folder to browse (first tap asks for access)", fontSize = 12.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(ROOTS) { root ->
                    val doc = remember(root.label, refreshTick) { rootDoc(root.label) }
                    val count = remember(doc, refreshTick) {
                        try { doc?.listFiles()?.size } catch (_: Exception) { null }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val d = rootDoc(root.label)
                                if (d != null) stack = listOf(d)
                                else { grantFor = root; treeLauncher.launch(null) }
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(BlueCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = BlueText, modifier = Modifier.size(22.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(root.label, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = TextDark)
                            Text(
                                if (doc == null) "Tap to grant access" else "${count ?: "–"} items",
                                fontSize = 12.sp, color = TextMuted
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            // file list of current dir
            val list = children
            when {
                list == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent)
                }
                list.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Folder is empty", fontSize = 14.sp, color = TextMuted)
                }
                else -> LazyColumn {
                    items(list, key = { it.uri.toString() }) { doc ->
                        val isDir = doc.isDirectory
                        val icon = when {
                            isDir -> Icons.Default.Folder
                            else -> when (kindFromName(doc.name ?: "")) {
                                Kind.IMAGE -> Icons.Default.Image
                                Kind.VIDEO -> Icons.Default.Videocam
                                Kind.AUDIO -> Icons.Default.MusicNote
                                Kind.DOC -> Icons.Default.Description
                            }
                        }
                        val sub = if (isDir) {
                            "Folder"
                        } else {
                            val d = try {
                                SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(doc.lastModified()))
                            } catch (_: Exception) { "" }
                            "${formatFileSize(doc.length())} · $d"
                        }
                        FileRowItem(
                            iconBg = if (isDir) BlueCard else GreenCard,
                            icon = icon,
                            iconTint = if (isDir) BlueText else GreenText,
                            title = doc.name ?: "unnamed",
                            subtitle = sub,
                            onClick = {
                                if (isDir) {
                                    stack = stack + doc
                                } else {
                                    val mime = try {
                                        ctx.contentResolver.getType(doc.uri)
                                    } catch (_: Exception) { null }
                                        ?: "application/octet-stream"
                                    val item = MediaItem(
                                        uri = doc.uri,
                                        name = doc.name ?: "file",
                                        size = doc.length(),
                                        date = doc.lastModified(),
                                        mime = mime,
                                        kind = kindFromMime(mime).let {
                                            if (it == Kind.DOC) kindFromName(doc.name ?: "") else it
                                        }
                                    )
                                    val r = item.toRoute()
                                    if (r != null) go(r) else onToast("Preview not available for this file")
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

