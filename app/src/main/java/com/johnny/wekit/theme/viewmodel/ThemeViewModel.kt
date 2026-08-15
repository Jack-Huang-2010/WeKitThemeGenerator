package com.johnny.wekit.theme.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.johnny.wekit.theme.data.ThemeColors
import com.johnny.wekit.theme.data.ThemeManifest
import com.johnny.wekit.theme.data.ThemeProject
import com.johnny.wekit.theme.data.ThemeStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ThemeViewModel : ViewModel() {

    private val _project = MutableStateFlow(ThemeProject())
    val project: StateFlow<ThemeProject> = _project.asStateFlow()

    // --- Manifest ---

    fun updateManifest(manifest: ThemeManifest) {
        _project.update { it.copy(manifest = manifest) }
    }

    fun updateManifestName(name: String) {
        _project.update { it.copy(manifest = it.manifest.copy(name = name)) }
    }

    fun updateManifestAuthor(author: String) {
        _project.update { it.copy(manifest = it.manifest.copy(author = author)) }
    }

    fun updateManifestVersion(version: String) {
        _project.update { it.copy(manifest = it.manifest.copy(version = version)) }
    }

    fun updateManifestDescription(description: String) {
        _project.update { it.copy(manifest = it.manifest.copy(description = description)) }
    }

    // --- Colors ---

    fun updateColor(key: String, value: String) {
        _project.update { it.copy(colors = it.colors.toMutableMap().apply { put(key, value) }) }
    }

    fun resetColors() {
        _project.update { it.copy(colors = ThemeColors.DEFAULT_COLORS) }
    }

    fun importColors(colors: Map<String, String>) {
        _project.update { project ->
            val merged = project.colors.toMutableMap()
            colors.forEach { (key, value) ->
                if (key in ThemeColors.ALL_KEYS) {
                    merged[key] = value
                }
            }
            project.copy(colors = merged)
        }
    }

    // --- Strings ---

    fun updateString(key: String, value: String) {
        _project.update { it.copy(strings = it.strings.toMutableMap().apply { put(key, value) }) }
    }

    fun resetStrings() {
        _project.update { it.copy(strings = ThemeStrings.DEFAULT_STRINGS) }
    }

    fun importStrings(strings: Map<String, String>) {
        _project.update { project ->
            val merged = project.strings.toMutableMap()
            strings.forEach { (key, value) ->
                if (key in ThemeStrings.ALL_KEYS) {
                    merged[key] = value
                }
            }
            project.copy(strings = merged)
        }
    }

    // --- Images ---

    fun setImage(path: String, uri: Uri) {
        _project.update { it.copy(images = it.images.toMutableMap().apply { put(path, uri) }) }
    }

    fun clearImage(path: String) {
        _project.update { it.copy(images = it.images.toMutableMap().apply { remove(path) }) }
    }

    fun clearAllImages() {
        _project.update { it.copy(images = emptyMap()) }
    }

    fun batchImportImages(mapping: Map<String, Uri>) {
        _project.update { project ->
            val merged = project.images.toMutableMap()
            mapping.forEach { (path, uri) ->
                merged[path] = uri
            }
            project.copy(images = merged)
        }
    }
}
