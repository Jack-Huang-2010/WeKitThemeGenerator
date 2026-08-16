package com.johnny.wekit.theme.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ThemeColors
import com.johnny.wekit.theme.util.DisplayName
import org.json.JSONObject

@Composable
fun ColorsEditorScreen(
    colors: Map<String, String>,
    onColorUpdate: (String, String) -> Unit,
    onResetColors: () -> Unit,
    onImportColors: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    var showColorPicker by remember { mutableStateOf<String?>(null) }
    val grouped = remember { ThemeColors.groupBySubCategory() }

    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                json?.let { text ->
                    val obj = JSONObject(text)
                    val imported = mutableMapOf<String, String>()
                    obj.keys().forEach { key ->
                        imported[key] = obj.getString(key)
                    }
                    onImportColors(imported)
                }
            } catch (_: Exception) {
                // Ignore parse errors
            }
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val json = JSONObject()
                colors.forEach { (key, value) ->
                    json.put(key, value)
                }
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(json.toString(2).toByteArray())
                }
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    // Color picker dialog
    showColorPicker?.let { colorKey ->
        ColorPickerDialog(
            initialColor = colors[colorKey] ?: "000000",
            onConfirm = { newValue ->
                onColorUpdate(colorKey, newValue)
                showColorPicker = null
            },
            onDismiss = {
                showColorPicker = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top bar with actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("颜色编辑", style = MaterialTheme.typography.headlineMedium)
            Row {
                TopBarAction(
                    tooltip = "重置",
                    icon = Icons.Outlined.Refresh,
                    variant = ActionVariant.Outlined,
                    onClick = onResetColors
                )
                Spacer(modifier = Modifier.width(8.dp))
                TopBarAction(
                    tooltip = "导入",
                    icon = Icons.Filled.FileUpload,
                    variant = ActionVariant.Filled,
                    onClick = { importLauncher.launch(arrayOf("application/json")) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                TopBarAction(
                    tooltip = "导出",
                    icon = Icons.Filled.FileDownload,
                    variant = ActionVariant.Filled,
                    onClick = { exportLauncher.launch("colors.json") }
                )
            }
        }

        // Color list grouped by sub-category
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            val sortedGroups = grouped.entries.sortedBy { (groupKey, _) ->
                when {
                    groupKey.startsWith("home") -> 0
                    groupKey.startsWith("chat") -> 1
                    groupKey.startsWith("settings") -> 2
                    else -> 3
                }
            }

            sortedGroups.forEach { (groupKey, keys) ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DisplayName.colorGroupName(LocalContext.current, groupKey),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    keys.forEach { key ->
                        val colorValue = colors[key] ?: "000000"
                        val previewColor = try {
                            val parsed = parseHexColor(colorValue)
                            Color(parsed)
                        } catch (_: Exception) {
                            Color.Black
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showColorPicker = key }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color preview box
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(previewColor)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Key name：中文化（找不到时 fallback 原始 key）
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = DisplayName.colorKeyName(LocalContext.current, key),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = key,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Color value
                            Text(
                                text = colorValue.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (key != keys.last()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun parseHexColor(hex: String): Int {
    val clean = hex.removePrefix("#").trim()
    return when (clean.length) {
        6 -> "FF$clean".toLong(16).toInt()
        8 -> clean.toLong(16).toInt()
        else -> 0xFF000000.toInt()
    }
}

private enum class ActionVariant { Filled, Outlined }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarAction(
    tooltip: String,
    icon: ImageVector,
    variant: ActionVariant,
    onClick: () -> Unit
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(tooltip) },
        state = tooltipState
    ) {
        val longPressModifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    tryAwaitRelease()
                    tooltipState.dismiss()
                },
                onLongPress = { tooltipState.show() }
            )
        }
        when (variant) {
            ActionVariant.Filled -> FilledIconButton(
                onClick = onClick,
                modifier = longPressModifier
            ) {
                Icon(icon, contentDescription = tooltip)
            }

            ActionVariant.Outlined -> OutlinedIconButton(
                onClick = onClick,
                modifier = longPressModifier
            ) {
                Icon(icon, contentDescription = tooltip)
            }
        }
    }
}
