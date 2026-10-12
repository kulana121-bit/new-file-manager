package com.ravi.newfilemanager.ui

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravi.newfilemanager.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class BrowserUiState(
    val currentPath: String = Environment.getExternalStorageDirectory().absolutePath,
    val files: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val breadcrumbs: List<String> = emptyList()
)

class FileBrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    init {
        loadDirectory(Environment.getExternalStorageDirectory().absolutePath)
    }

    fun loadDirectory(path: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val files = withContext(Dispatchers.IO) {
                try {
                    val dir = File(path)
                    if (!dir.exists() || !dir.isDirectory) return@withContext emptyList<FileItem>()
                    dir.listFiles()
                        ?.filter { !it.name.startsWith(".") }
                        ?.map { FileItem(it) }
                        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                        ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val crumbs = path
                .removePrefix(Environment.getExternalStorageDirectory().absolutePath)
                .split("/")
                .filter { it.isNotEmpty() }
            _uiState.value = BrowserUiState(
                currentPath = path,
                files = files,
                isLoading = false,
                breadcrumbs = listOf("Storage") + crumbs
            )
        }
    }

    fun navigateUp() {
        val current = _uiState.value.currentPath
        val parent = File(current).parent
        val root = Environment.getExternalStorageDirectory().absolutePath
        if (parent != null && current != root && current.startsWith(root)) {
            loadDirectory(parent)
        }
    }

    fun canGoUp(): Boolean {
        val current = _uiState.value.currentPath
        val root = Environment.getExternalStorageDirectory().absolutePath
        return current != root && current.startsWith(root)
    }
}
