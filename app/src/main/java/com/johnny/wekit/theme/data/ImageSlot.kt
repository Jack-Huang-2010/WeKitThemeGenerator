package com.johnny.wekit.theme.data

/**
 * Represents a single image slot in the WeKit theme package.
 * @param path Relative path within the theme, e.g. "home/background.png"
 * @param displayName Human-readable name for the image, e.g. "background"
 * @param category Top-level category, e.g. "home", "chat", "plus", "settings", "splash"
 */
data class ImageSlot(
    val path: String,
    val displayName: String,
    val category: String
)
