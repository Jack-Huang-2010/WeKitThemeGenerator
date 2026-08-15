package com.johnny.wekit.theme.ui

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.johnny.wekit.theme.data.ThemeProject
import com.johnny.wekit.theme.util.ImageSlotTree
import com.johnny.wekit.theme.util.ThemeExporter
import java.io.File

@Composable
fun ExportScreen(
    project: ThemeProject,
    onExport: (Context) -> File
) {
    val context = LocalContext.current
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("生成导出", style = MaterialTheme.typography.headlineMedium)

        // Theme summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("主题概览", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                SummaryRow("名称", project.manifest.name.ifBlank { "未命名" })
                SummaryRow("作者", project.manifest.author.ifBlank { "未填写" })
                SummaryRow("版本", project.manifest.version)
                SummaryRow("颜色数", project.colors.size.toString())
                SummaryRow("字符串数", project.strings.size.toString())
                SummaryRow("已替换图片", "${project.images.size}/${ImageSlotTree.ALL_SLOTS.size}")
            }
        }

        // Export button
        Button(
            onClick = {
                isExporting = true
                try {
                    val file = onExport(context)
                    exportedFile = file
                    Toast.makeText(context, "导出成功: ${file.name}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isExporting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isExporting && project.manifest.name.isNotBlank()
        ) {
            Text(if (isExporting) "正在生成..." else "生成主题包")
        }

        if (project.manifest.name.isBlank()) {
            Text(
                "⚠️ 请先在「主题信息」页面填写主题名称",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Save to Downloads
        exportedFile?.let { file ->
            OutlinedButton(
                onClick = {
                    val uri = ThemeExporter.saveToDownloads(context, file)
                    if (uri != null) {
                        Toast.makeText(context, "已保存到 Downloads", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存到 Downloads")
            }

            // Share
            OutlinedButton(
                onClick = {
                    try {
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "分享主题包"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "分享失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("分享主题包")
            }
        }

        // Directory structure preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("目录结构预览", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                val themeName = project.manifest.name.ifBlank { "主题名" }
                Text("$themeName/", style = MaterialTheme.typography.bodySmall)
                Text("  ├── manifest.json", style = MaterialTheme.typography.bodySmall)
                Text("  ├── colors.json", style = MaterialTheme.typography.bodySmall)
                Text("  ├── strings.json", style = MaterialTheme.typography.bodySmall)

                // Show replaced images
                val replacedImages = project.images.keys.sorted()
                if (replacedImages.isNotEmpty()) {
                    replacedImages.forEachIndexed { index, path ->
                        val prefix = if (index == replacedImages.lastIndex) "└──" else "├──"
                        Text("  $prefix $path", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Text("  └── (暂无图片)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
