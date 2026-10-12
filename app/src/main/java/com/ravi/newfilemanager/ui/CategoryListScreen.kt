package com.ravi.newfilemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun CategoryListScreen(
    title: String,
    kind: Kind,
    go: (Route) -> Unit,
    onBack: () -> Unit,
    onToast: (String) -> Unit
) {
    val ctx = LocalContext.current
    var items by remember { mutableStateOf<List<MediaItem>?>(null) }
    LaunchedEffect(kind) {
        items = withContext(Dispatchers.IO) {
            when (kind) {
                Kind.IMAGE -> MediaStoreRepo.images(ctx)
                Kind.VIDEO -> MediaStoreRepo.videos(ctx)
                Kind.AUDIO -> MediaStoreRepo.audio(ctx)
                Kind.DOC -> MediaStoreRepo.documents(ctx)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BgColor).padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        ScreenHeader(title = title, onBack = onBack)

        val list = items
        when {
            list == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
            list.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No $title found", fontSize = 14.sp, color = TextMuted)
            }
            else -> LazyColumn {
                item { Spacer(modifier = Modifier.height(6.dp)) }
                items(list) { item ->
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
                            if (r != null) {
                                // pass full list so viewer can swipe
                                go(r.copy(items = list, index = list.indexOf(item)))
                            } else onToast("Preview not available for this file")
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}
