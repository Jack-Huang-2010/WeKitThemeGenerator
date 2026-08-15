package com.johnny.wekit.theme.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.johnny.wekit.theme.data.ThemeManifest
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
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
        val themeName = manifest.name.ifBlank { "UntitledTheme" }
        val zipFile = File(context.cacheDir, "$themeName.wekit.zip")

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            // manifest.json
            val manifestJson = JSONObject().apply {
                put("name", manifest.name)
                put("author", manifest.author)
                put("version", manifest.version)
                put("description", manifest.description)
            }
            addTextEntry(zos, "$themeName/manifest.json", manifestJson.toString(2))

            // colors.json
            val colorsJson = JSONObject()
            colors.forEach { (key, value) ->
                colorsJson.put(key, value)
            }
            addTextEntry(zos, "$themeName/colors.json", colorsJson.toString(2))

            // strings.json
            val stringsJson = JSONObject()
            strings.forEach { (key, value) ->
                stringsJson.put(key, value)
            }
            addTextEntry(zos, "$themeName/strings.json", stringsJson.toString(2))

            // Images - only include replaced ones
            images.forEach { (path, uri) ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        zos.putNextEntry(ZipEntry("$themeName/$path"))
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

    /**
     * Save the zip file to the Downloads directory using MediaStore (API 29+)
     * or legacy file copy (API 26-28).
     * @return The content URI of the saved file, or null on failure
     */
    fun saveToDownloads(context: Context, zipFile: File): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsMediaStore(context, zipFile)
        } else {
            saveToDownloadsLegacy(context, zipFile)
        }
    }

    /**
     * API 29+: Use MediaStore to save to Downloads.
     */
    private fun saveToDownloadsMediaStore(context: Context, zipFile: File): Uri? {
        return try {
            val fileName = zipFile.name
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val collection = MediaStore.Downloads.getContentUri("externalPrimary")
            val uri = context.contentResolver.insert(collection, values) ?: return null

            context.contentResolver.openOutputStream(uri)?.use { output ->
                FileInputStream(zipFile).use { input ->
                    input.copyTo(output)
                }
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(uri, values, null, null)

            uri
        } catch (_: Exception) {
            null
        }
    }

    /**
     * API 26-28: Use legacy Environment.getExternalStoragePublicDirectory.
     */
    @Suppress("DEPRECATION")
    private fun saveToDownloadsLegacy(context: Context, zipFile: File): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val destFile = File(downloadsDir, zipFile.name)
            FileInputStream(zipFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (_: Exception) {
            null
        }
    }

    private fun addTextEntry(zos: ZipOutputStream, entryName: String, content: String) {
        zos.putNextEntry(ZipEntry(entryName))
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }
}
