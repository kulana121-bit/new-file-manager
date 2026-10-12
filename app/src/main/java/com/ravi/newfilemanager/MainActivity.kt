package com.ravi.newfilemanager

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ravi.newfilemanager.data.Kind
import com.ravi.newfilemanager.data.MediaItem
import com.ravi.newfilemanager.data.Prefs
import com.ravi.newfilemanager.data.VaultManager
import com.ravi.newfilemanager.ui.AudioPlayerScreen
import com.ravi.newfilemanager.ui.BrowseScreen
import com.ravi.newfilemanager.ui.CategoryListScreen
import com.ravi.newfilemanager.ui.DashboardScreen
import com.ravi.newfilemanager.ui.HtmlViewerScreen
import com.ravi.newfilemanager.ui.InternalStorageScreen
import com.ravi.newfilemanager.ui.LocalDarkMode
import com.ravi.newfilemanager.ui.LockedFilesScreen
import com.ravi.newfilemanager.ui.PdfReaderScreen
import com.ravi.newfilemanager.ui.PhotoViewerScreen
import com.ravi.newfilemanager.ui.PrivatePhotosScreen
import com.ravi.newfilemanager.ui.VaultCategoryScreen
import com.ravi.newfilemanager.ui.Route
import com.ravi.newfilemanager.ui.SearchScreen
import com.ravi.newfilemanager.ui.SettingsScreen
import com.ravi.newfilemanager.ui.VaultUnlockedScreen
import com.ravi.newfilemanager.ui.VideoPlayerScreen
import com.ravi.newfilemanager.ui.theme.NewFileManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FileManagerApp()
        }
    }
}

@Composable
fun FileManagerApp() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    var dark by remember { mutableStateOf(prefs.darkMode) }
    val stack = remember { mutableStateListOf(Route("dashboard")) }
    var vaultPin by remember { mutableStateOf<String?>(null) }
    var pendingLock by remember { mutableStateOf<MediaItem?>(null) }
    val scope = rememberCoroutineScope()

    val onToast: (String) -> Unit = { msg ->
        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    }

    fun go(r: Route) {
        stack.add(r)
    }
    fun back() {
        if (stack.size > 1) stack.removeLast()
    }
    val route = stack.last()

    // System back / gesture: go to previous screen instead of closing the app.
    // Only the dashboard (root) closes the app.
    BackHandler(enabled = stack.size > 1) {
        back()
    }

    // Lock a normal file into the vault (viewers call this; PIN collected via locked screen)
    fun requestLock(item: MediaItem) {
        pendingLock = item
        go(Route("locked"))
    }
    fun doLock(item: MediaItem, pin: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val cat = when (item.kind) {
                    Kind.IMAGE -> "photos"
                    Kind.VIDEO -> "videos"
                    Kind.AUDIO -> "audio"
                    Kind.DOC -> "docs"
                }
                VaultManager(ctx).lockUri(item.uri, item.name, item.mime, cat, pin)
                try { ctx.contentResolver.delete(item.uri, null, null) } catch (_: Exception) { }
                withContext(Dispatchers.Main) {
                    pendingLock = null
                    onToast("Locked into vault")
                    go(Route("vault"))
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    pendingLock = null
                    onToast("Could not lock file")
                }
            }
        }
    }

    // storage + camera permissions up front
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    LaunchedEffect(Unit) {
        permLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
    }

    CompositionLocalProvider(LocalDarkMode provides dark) {
        NewFileManagerTheme(darkTheme = dark) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                when (route.name) {
                    "dashboard" -> DashboardScreen(go = ::go, onToast = onToast)
                    "browse" -> BrowseScreen(go = ::go, onToast = onToast)
                    "search" -> SearchScreen(go = ::go, onBack = ::back, onToast = onToast)
                    "settings" -> SettingsScreen(
                        go = ::go, onToast = onToast, dark = dark,
                        onDarkChange = { dark = it }
                    )
                    "locked" -> LockedFilesScreen(
                        onBack = ::back,
                        onUnlock = { pin ->
                            vaultPin = pin
                            val item = pendingLock
                            if (item != null) doLock(item, pin) else go(Route("vault"))
                        }
                    )
                    "vault" -> {
                        val pin = vaultPin
                        if (pin != null) {
                            VaultUnlockedScreen(
                                pin = pin,
                                onBack = ::back,
                                onCategoryClick = { cat -> go(Route("vault_category", category = cat)) },
                                go = ::go,
                                onToast = onToast
                            )
                        } else {
                            LockedFilesScreen(
                                onBack = ::back,
                                onUnlock = { p -> vaultPin = p; go(Route("vault")) }
                            )
                        }
                    }
                    "vault_category" -> {
                        val pin = vaultPin
                        if (pin != null && route.category.isNotBlank()) {
                            VaultCategoryScreen(
                                pin = pin, category = route.category,
                                onBack = ::back, go = ::go, onToast = onToast
                            )
                        } else {
                            LockedFilesScreen(
                                onBack = ::back,
                                onUnlock = { p -> vaultPin = p; go(Route("vault")) }
                            )
                        }
                    }
                    "internal" -> InternalStorageScreen(go = ::go, onBack = ::back, onToast = onToast)
                    "category" -> CategoryListScreen(
                        title = route.title, kind = route.kind,
                        go = ::go, onBack = ::back, onToast = onToast
                    )
                    "photo" -> PhotoViewerScreen(
                        items = route.items, startIndex = route.index,
                        onBack = ::back, onToast = onToast, onLockRequest = ::requestLock
                    )
                    "video" -> route.item?.let {
                        VideoPlayerScreen(item = it, onBack = ::back, onToast = onToast, onLockRequest = ::requestLock)
                    }
                    "audio" -> route.item?.let {
                        AudioPlayerScreen(item = it, onBack = ::back, onToast = onToast, onLockRequest = ::requestLock)
                    }
                    "pdf" -> route.item?.let {
                        PdfReaderScreen(item = it, onBack = ::back, onToast = onToast, onLockRequest = ::requestLock)
                    }
                    "html" -> route.item?.let {
                        HtmlViewerScreen(item = it, onBack = ::back, onToast = onToast, onLockRequest = ::requestLock)
                    }
                }
            }
        }
    }
}
