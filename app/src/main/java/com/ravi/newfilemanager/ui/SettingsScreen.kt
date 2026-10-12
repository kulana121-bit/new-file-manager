package com.ravi.newfilemanager.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.CloudProviders
import com.ravi.newfilemanager.data.Prefs
import com.ravi.newfilemanager.data.StorageStats
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    go: (Route) -> Unit,
    onToast: (String) -> Unit,
    dark: Boolean,
    onDarkChange: (Boolean) -> Unit
) {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var cacheSize by remember { mutableStateOf(0L) }
    var showAutoLock by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var showBreakdown by remember { mutableStateOf(false) }
    var showCloudSoon by remember { mutableStateOf(false) }
    var storage by remember { mutableStateOf(StorageStats.Info(0, 0)) }

    fun refreshCache() {
        cacheSize = try {
            ctx.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } catch (_: Exception) { 0L }
    }
    LaunchedEffect(Unit) {
        refreshCache()
        storage = withContext(Dispatchers.IO) { StorageStats.primary() }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            TopTitle("Settings")

            LazyColumn(modifier = Modifier.weight(1f)) {
                // SECURITY & VAULT
                item {
                    GroupLabel("SECURITY & VAULT")
                    CardBox {
                        SettingRow(
                            icon = Icons.Default.Lock, iconBg = PinkCard, iconTint = PinkText,
                            title = "Passcode & Face ID", value = if (prefs.hasPin) "On" else "Off",
                            onClick = { go(Route("locked")) }
                        )
                        SettingRow(
                            icon = Icons.Default.Timer, iconBg = BlueCard, iconTint = BlueText,
                            title = "Auto-Lock Timer",
                            value = when (prefs.autoLockMinutes) {
                                0 -> "Immediately"; 1 -> "After 1 minute"; 5 -> "After 5 minutes"; else -> "After 15 minutes"
                            },
                            onClick = { showAutoLock = true }
                        )
                    }
                }
                // STORAGE & CLOUD
                item {
                    GroupLabel("STORAGE & CLOUD")
                    CardBox {
                        SettingRow(
                            icon = Icons.Default.Cloud, iconBg = GreenCard, iconTint = GreenText,
                            title = "Manage Cloud Drives",
                            value = if (CloudProviders.current.isAvailable) "Connected" else "Google Drive, iCloud",
                            onClick = {
                                if (CloudProviders.current.isAvailable) onToast("Cloud drives in next update")
                                else showCloudSoon = true
                            }
                        )
                        SettingRow(
                            icon = Icons.Default.CleaningServices, iconBg = CreamCard, iconTint = CreamText,
                            title = "Clear App Cache", value = formatFileSize(cacheSize),
                            onClick = {
                                try {
                                    ctx.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                                } catch (_: Exception) { }
                                refreshCache()
                                onToast("Cache cleared")
                            }
                        )
                        SettingRow(
                            icon = Icons.Default.Storage, iconBg = BlueCard, iconTint = BlueText,
                            title = "Storage Breakdown",
                            onClick = { showBreakdown = true }
                        )
                    }
                }
                // PREFERENCES & SUPPORT
                item {
                    GroupLabel("PREFERENCES & SUPPORT")
                    CardBox {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(13.dp)
                        ) {
                            SettingIcon(Icons.Default.DarkMode, CreamCard, CreamText)
                            Text("Dark Mode", fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = TextDark,
                                modifier = Modifier.weight(1f))
                            Switch(
                                checked = dark,
                                onCheckedChange = {
                                    onDarkChange(it)
                                    prefs.darkMode = it
                                    onToast(if (it) "Dark mode on" else "Dark mode off")
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent)
                            )
                        }
                        SettingRow(
                            icon = Icons.Default.Help, iconBg = PurpleCard, iconTint = PurpleText,
                            title = "Help & Support",
                            onClick = { onToast("Help & Support in next update") }
                        )
                        SettingRow(
                            icon = Icons.Default.Info, iconBg = BlueCard, iconTint = BlueText,
                            title = "About", value = "v1.0.0",
                            onClick = { showAbout = true }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(86.dp)) }
            }
        }
        BottomDock(
            selected = "settings",
            onHome = { go(Route("dashboard")) },
            onBrowse = { go(Route("browse")) },
            onSettings = {}
        )
    }

    if (showAutoLock) {
        AlertDialog(
            onDismissRequest = { showAutoLock = false },
            title = { Text("Auto-Lock Timer") },
            text = {
                Column {
                    listOf(0 to "Immediately", 1 to "After 1 minute", 5 to "After 5 minutes", 15 to "After 15 minutes")
                        .forEach { (min, label) ->
                            Text(
                                label, fontSize = 15.sp, color = TextDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        prefs.autoLockMinutes = min
                                        showAutoLock = false
                                        onToast("Auto-lock: $label")
                                    }
                                    .padding(vertical = 10.dp)
                            )
                        }
                }
            },
            confirmButton = { TextButton(onClick = { showAutoLock = false }) { Text("Cancel") } }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("Files v1.0.0") },
            text = { Text("A clean, fast file manager with a private encrypted vault.\n\nBuilt for Ravi.") },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("OK") } }
        )
    }

    if (showBreakdown) {
        AlertDialog(
            onDismissRequest = { showBreakdown = false },
            title = { Text("Storage Breakdown") },
            text = {
                Text(
                    "Used: ${formatFileSize(storage.usedBytes)}\n" +
                    "Free: ${formatFileSize(storage.freeBytes)}\n" +
                    "Total: ${formatFileSize(storage.totalBytes)}\n\n" +
                    "Detailed per-app breakdown in next update."
                )
            },
            confirmButton = { TextButton(onClick = { showBreakdown = false }) { Text("OK") } }
        )
    }

    if (showCloudSoon) {
        AlertDialog(
            onDismissRequest = { showCloudSoon = false },
            title = { Text("Cloud Drive") },
            text = { Text(CloudProviders.current.comingSoonMessage) },
            confirmButton = { TextButton(onClick = { showCloudSoon = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun CardBox(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(CardColor)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) { content() }
}

@Composable
private fun SettingIcon(icon: ImageVector, bg: Color, tint: Color) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector, iconBg: Color, iconTint: Color,
    title: String, value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        SettingIcon(icon, iconBg, iconTint)
        Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = TextDark, modifier = Modifier.weight(1f))
        if (value != null) Text(value, fontSize = 12.5.sp, color = TextMuted)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}
