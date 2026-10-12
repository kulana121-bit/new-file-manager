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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.MediaStoreRepo
import com.ravi.newfilemanager.data.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen(
    go: (Route) -> Unit,
    onBack: () -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    var query by remember { mutableStateOf("") }
    var chip by remember { mutableStateOf<Kind?>(null) } // null = All
    var results by remember { mutableStateOf<List<MediaItem>>(emptyList()) }

    LaunchedEffect(query) {
        results = withContext(Dispatchers.IO) {
            val all = MediaStoreRepo.search(ctx, query)
            if (chip == null) all else all.filter { it.kind == chip }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            TopTitle("Search")

            // search field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(CardColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search files, folders...", fontSize = 14.sp, color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    "Cancel", fontSize = 14.sp, color = TextMuted,
                    modifier = Modifier.clickable { onBack() }.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val chips = listOf(null to "All", Kind.DOC to "Documents", Kind.IMAGE to "Images", Kind.VIDEO to "Videos", Kind.AUDIO to "Music")
                items(chips) { (k, label) ->
                    val on = chip == k
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (on) BlueCard else CardColor)
                            .clickable { chip = k }
                            .padding(horizontal = 15.dp, vertical = 8.dp)
                    ) {
                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                            color = if (on) BlueText else TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                if (query.isBlank()) {
                    item {
                        Text("Type to search across your device", fontSize = 13.sp, color = TextMuted,
                            modifier = Modifier.padding(vertical = 24.dp))
                    }
                } else if (results.isEmpty()) {
                    item {
                        Text("No results for \"$query\"", fontSize = 13.sp, color = TextMuted,
                            modifier = Modifier.padding(vertical = 24.dp))
                    }
                } else {
                    items(results) { item ->
                        val date = remember(item.date) {
                            SimpleDateFormat("MMM yyyy", Locale.US).format(Date(item.date))
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
                            }
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Secured Storage is never included in search", fontSize = 12.sp, color = TextMuted)
                    }
                    Spacer(modifier = Modifier.height(86.dp))
                }
            }
        }
        BottomDock(
            modifier = Modifier.align(Alignment.BottomCenter),
            selected = "",
            onHome = { go(Route("dashboard")) },
            onBrowse = { go(Route("browse")) },
            onSettings = { go(Route("settings")) }
        )
    }
}
