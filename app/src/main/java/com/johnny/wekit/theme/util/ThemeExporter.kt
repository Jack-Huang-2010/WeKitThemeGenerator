package com.johnny.wekit.theme.util

import android.content.Context
import android.net.Uri
import com.johnny.wekit.theme.data.ThemeManifest
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Exports a WeKit theme project as a zip file.
 */
object ThemeExporter {

    /**
     * Export the theme project to a zip file.
     * @param context Android context
     * @param manifest Theme manifest
     * @param colors Color map
     * @param strings String map
     * @param images Image URI map (path -> content URI)
     * @return The generated zip file
     */
    fun export(
        context: Context,
        manifest: ThemeManifest,
        colors: Map<String, String>,
        strings: Map<String, String>,
        images: Map<String, Uri>
    ): File {
        val themeName = manifest.name
            .ifBlank { "UntitledTheme" }
            // 清理文件名/zip 条目中的非法字符
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val zipFile = File(context.cacheDir, "$themeName.wekit.zip")
        val root = "$themeName/"

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // manifest.json
            val manifestJson = JSONObject().apply {
                put("name", manifest.name)
                put("author", manifest.author)
                put("version", manifest.version)
                put("description", manifest.description)
            }
            addTextEntry(zos, "${root}manifest.json", manifestJson.toString(2))

            // colors.json
            val colorsJson = JSONObject()
            colors.forEach { (key, value) ->
                colorsJson.put(key, value)
            }
            addTextEntry(zos, "${root}colors.json", colorsJson.toString(2))

            // strings.json
            val stringsJson = JSONObject()
            strings.forEach { (key, value) ->
                stringsJson.put(key, value)
            }
            addTextEntry(zos, "${root}strings.json", stringsJson.toString(2))

            // 场景目录骨架（严格按模板格式：home/、chat/、chat/bubbles/、plus/、settings/、splash/ 等，
            // 即使没有替换图片也保留目录结构）
            ImageSlotTree.ALL_SLOTS
                .map { it.path.substringBeforeLast("/") + "/" }
                .distinct()
                .sorted()
                .forEach { dir ->
                    zos.putNextEntry(ZipEntry(root + dir))
                    zos.closeEntry()
                }

            // Images - only include replaced ones
            images.forEach { (path, uri) ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        zos.putNextEntry(ZipEntry(root + path))
                        input.copyTo(zos)
                        zos.closeEntry()
                    }
                } catch (_: Exception) {
                    // Skip images that can't be read
                }
            }
        }

        return zipFile
    }

    private fun addTextEntry(zos: ZipOutputStream, entryName: String, content: String) {
        zos.putNextEntry(ZipEntry(entryName))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}
