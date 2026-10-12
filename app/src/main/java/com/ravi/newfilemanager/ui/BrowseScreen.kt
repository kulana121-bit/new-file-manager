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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ravi.newfilemanager.data.CloudProviders
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.StorageStats
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class CatDef(
    val title: String, val icon: ImageVector,
    val bg: Color, val tint: Color, val kind: Kind?
)

@Composable
fun BrowseScreen(
    go: (Route) -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    var storage by remember { mutableStateOf(StorageStats.Info(0, 0)) }
    LaunchedEffect(Unit) {
        storage = withContext(Dispatchers.IO) { StorageStats.primary() }
    }
    val cloud = CloudProviders.current

    val cats = listOf(
        CatDef("Images", Icons.Default.Image, PeachCard, PeachText, Kind.IMAGE),
        CatDef("Videos", Icons.Default.Videocam, PurpleCard, PurpleText, Kind.VIDEO),
        CatDef("Documents", Icons.Default.Description, BlueCard, BlueText, Kind.DOC),
        CatDef("Music", Icons.Default.MusicNote, GreenCard, GreenText, Kind.AUDIO),
        CatDef("Downloads", Icons.Default.Download, CreamCard, CreamText, null),
        CatDef("Archives", Icons.Default.Archive, PinkCard, PinkText, null)
    )

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            TopTitle("Browse")
            SearchPill("Search files, folders...") { go(Route("search")) }

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    SectionTitle("Categories")
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        cats.chunked(3).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                row.forEach { c ->
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .shadow(4.dp, RoundedCornerShape(20.dp))
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(CardColor)
                                            .clickable {
                                                if (c.kind != null) go(Route("category", title = c.title, kind = c.kind))
                                                else onToast("${c.title} view in next update")
                                            }
                                            .padding(vertical = 16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(17.dp))
                                                .background(c.bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(c.icon, contentDescription = null, tint = c.tint, modifier = Modifier.size(26.dp))
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(c.title, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    SectionTitle("Locations")
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardColor)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        LocationRow(
                            icon = Icons.Default.Smartphone, iconBg = BlueCard, iconTint = BlueText,
                            title = "Internal Storage",
                            subtitle = "${formatFileSize(storage.freeBytes)} free / ${formatFileSize(storage.totalBytes)}",
                            onClick = { go(Route("internal")) }
                        )
                        LocationRow(
                            icon = Icons.Default.Lock, iconBg = PinkCard, iconTint = PinkText,
                            title = "Secured Storage",
                            subtitle = "Password locked",
                            onClick = { go(Route("locked")) }
                        )
                        LocationRow(
                            icon = Icons.Default.Cloud, iconBg = GreenCard, iconTint = GreenText,
                            title = "Cloud Drive",
                            subtitle = if (cloud.isAvailable) "Connected" else "Coming soon",
                            showChevron = cloud.isAvailable,
                            onClick = {
                                if (cloud.isAvailable) onToast("Cloud browser in next update")
                                else onToast(cloud.comingSoonMessage)
                            }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(86.dp)) }
            }
        }
        BottomDock(
            modifier = Modifier.align(Alignment.BottomCenter),
            selected = "browse",
            onHome = { go(Route("dashboard")) },
            onBrowse = {},
            onSettings = { go(Route("settings")) }
        )
    }
}

@Composable
private fun LocationRow(
    icon: ImageVector, iconBg: Color, iconTint: Color,
    title: String, subtitle: String,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = TextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
        if (showChevron) Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
    }
}
