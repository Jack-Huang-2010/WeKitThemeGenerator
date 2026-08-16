package com.johnny.wekit.theme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.johnny.wekit.theme.data.ThemeManifest

@Composable
fun ThemeEditorScreen(
    manifest: ThemeManifest,
    onManifestUpdate: (ThemeManifest) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "主题信息",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = manifest.name,
            onValueChange = { onManifestUpdate(manifest.copy(name = it)) },
            label = { Text("主题名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：暗夜主题") }
        )

        OutlinedTextField(
            value = manifest.author,
            onValueChange = { onManifestUpdate(manifest.copy(author = it)) },
            label = { Text("作者") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：Johnny") }
        )

        OutlinedTextField(
            value = manifest.version,
            onValueChange = { onManifestUpdate(manifest.copy(version = it)) },
            label = { Text("版本") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("例如：1.0") }
        )

        OutlinedTextField(
            value = manifest.description,
            onValueChange = { onManifestUpdate(manifest.copy(description = it)) },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            placeholder = { Text("主题描述...") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("预览", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("名称: ${manifest.name.ifBlank { "未命名" }}")
                Text("作者: ${manifest.author.ifBlank { "未填写" }}")
                Text("版本: ${manifest.version}")
                Text("描述: ${manifest.description.ifBlank { "无描述" }}")
            }
        }
    }
}
