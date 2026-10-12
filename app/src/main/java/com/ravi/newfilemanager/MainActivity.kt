package com.ravi.newfilemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ravi.newfilemanager.ui.DashboardScreen
import com.ravi.newfilemanager.ui.FileBrowserScreen
import com.ravi.newfilemanager.ui.LockedFilesScreen
import com.ravi.newfilemanager.ui.SettingsScreen
import com.ravi.newfilemanager.ui.VaultUnlockedScreen
import com.ravi.newfilemanager.ui.theme.NewFileManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewFileManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var screen by remember { mutableStateOf("dashboard") }

                    when (screen) {
                        "dashboard" -> DashboardScreen(
                            onNavigate = { screen = it }
                        )
                        "browser" -> FileBrowserScreen(
                            onBack = { screen = "dashboard" }
                        )
                        "settings" -> SettingsScreen(
                            onBack = { screen = "dashboard" },
                            onLockedFiles = { screen = "locked" }
                        )
                        "locked" -> LockedFilesScreen(
                            onBack = { screen = "settings" },
                            onUnlock = { screen = "vault" }
                        )
                        "vault" -> VaultUnlockedScreen(
                            onBack = { screen = "dashboard" }
                        )
                    }
                }
            }
        }
    }
}
