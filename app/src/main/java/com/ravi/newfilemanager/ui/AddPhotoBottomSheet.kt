package com.ravi.newfilemanager.ui

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.ravi.newfilemanager.data.CloudProviders
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.VaultManager
import com.ravi.newfilemanager.data.kindFromMime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AddPhotoBottomSheet(
    pin: String,
    category: String = "photos",
    onDismiss: () -> Unit,
    onLocked: () -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val vault = remember { VaultManager(ctx) }
    var busy by remember { mutableStateOf(false) }

    val cameraFile = remember { File(ctx.cacheDir, "cam_${System.currentTimeMillis()}.jpg") }
    val cameraUri = remember {
        FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", cameraFile)
    }

    fun lockUri(uri: Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                busy = true
                val name = queryName(ctx, uri) ?: "photo_${System.currentTimeMillis()}.jpg"
                val mime = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                val cat = when (kindFromMime(mime)) {
                    Kind.IMAGE -> "photos"
                    Kind.VIDEO -> "videos"
                    Kind.AUDIO -> "audio"
                    else -> "docs"
                }
                vault.lockUri(uri, name, mime, if (category == "photos") "photos" else cat, pin)
                try { ctx.contentResolver.delete(uri, null, null) } catch (_: Exception) { }
                withContext(Dispatchers.Main) {
                    busy = false
                    onLocked(); onDismiss(); onToast("Locked into vault")
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { busy = false; onToast("Could not lock file") }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) {
            scope.launch(Dispatchers.IO) {
                try {
                    busy = true
                    cameraFile.inputStream().use { ins ->
                        vault.lockStream(ins, cameraFile.name, "image/jpeg", "photos", pin)
                    }
                    cameraFile.delete()
                    withContext(Dispatchers.Main) {
                        busy = false; onLocked(); onDismiss(); onToast("Photo locked into vault")
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) { busy = false; onToast("Could not lock photo") }
                }
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) lockUri(uri) }
    val filesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) lockUri(uri) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { if (!busy) onDismiss() },
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
                Text("Add to Private Photos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LineColor)
                        .clickable { if (!busy) onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BottomSheetOption(
                icon = Icons.Default.CameraAlt, iconTint = PeachText, iconBg = PeachCard,
                title = "Take Photo", subtitle = "Use camera to capture directly into vault",
                onClick = { cameraLauncher.launch(cameraUri) }
            )
            BottomSheetOption(
                icon = Icons.Default.PhotoLibrary, iconTint = PurpleText, iconBg = PurpleCard,
                title = "Import from Gallery", subtitle = "Select from your device's library",
                onClick = { galleryLauncher.launch("image/*") }
            )
            BottomSheetOption(
                icon = Icons.Default.FolderOpen, iconTint = GreenText, iconBg = GreenCard,
                title = "Upload from Files", subtitle = "Choose from local device storage",
                onClick = { filesLauncher.launch(arrayOf("image/*")) }
            )
            BottomSheetOption(
                icon = Icons.Default.CloudUpload, iconTint = BlueText, iconBg = BlueCard,
                title = "Cloud Import", subtitle = "Fetch from linked drive or cloud",
                onClick = { onToast(CloudProviders.current.comingSoonMessage) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardColor)
                    .clickable { if (!busy) onDismiss() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (busy) "Locking..." else "Cancel",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark
                )
            }
        }
    }
}

@Composable
fun BottomSheetOption(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

fun queryName(ctx: Context, uri: Uri): String? {
    return try {
        ctx.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (_: Exception) { null }
}
