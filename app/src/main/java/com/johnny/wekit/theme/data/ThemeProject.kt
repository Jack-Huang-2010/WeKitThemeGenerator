package com.johnny.wekit.theme.data

import android.net.Uri

/**
 * Represents a complete WeKit theme project.
 */
data class ThemeProject(
    val manifest: ThemeManifest = ThemeManifest(),
    val colors: Map<String, String> = ThemeColors.DEFAULT_COLORS,
    val strings: Map<String, String> = ThemeStrings.DEFAULT_STRINGS,
    val images: Map<String, Uri> = emptyMap()
)
