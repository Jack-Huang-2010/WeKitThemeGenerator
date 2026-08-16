package com.johnny.wekit.theme.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.johnny.wekit.theme.util.ThemeExporter
import com.johnny.wekit.theme.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconOutlined: ImageVector
) {
    data object Theme : Screen("theme", "主题信息", Icons.Filled.Info, Icons.Outlined.Info)
    data object Colors : Screen("colors", "颜色", Icons.Filled.Palette, Icons.Outlined.Palette)
    data object Strings : Screen("strings", "字符串", Icons.Filled.TextFields, Icons.Outlined.TextFields)
    data object Images : Screen("images", "图片", Icons.Filled.Image, Icons.Outlined.Image)
    data object Export : Screen("export", "导出", Icons.Filled.Download, Icons.Outlined.Download)
    data object About : Screen("about", "关于", Icons.Filled.Info, Icons.Outlined.Info)
}

@Composable
fun ThemeNavigation(viewModel: ThemeViewModel = viewModel()) {
    val project by viewModel.project.collectAsState()
    val scope = rememberCoroutineScope()
    val screens = listOf(
        Screen.Theme,
        Screen.Colors,
        Screen.Strings,
        Screen.Images,
        Screen.Export
    )
    val pagerState = rememberPagerState(pageCount = { screens.size })
    var aboutVisible by rememberSaveable { mutableStateOf(false) }
    var backProgress by remember { mutableStateOf(0f) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEachIndexed { index, screen ->
                    val selected = pagerState.currentPage == index
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) screen.icon else screen.iconOutlined,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        alwaysShowLabel = false,
                        selected = selected,
                        onClick = {
                            aboutVisible = false
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !aboutVisible
            ) { page ->
                when (screens[page]) {
                    Screen.Theme -> ThemeEditorScreen(
                        manifest = project.manifest,
                        onManifestUpdate = { viewModel.updateManifest(it) },
                        onNavigateToAbout = { aboutVisible = true }
                    )
                    Screen.Colors -> ColorsEditorScreen(
                        colors = project.colors,
                        onColorUpdate = { key, value -> viewModel.updateColor(key, value) },
                        onResetColors = { viewModel.resetColors() },
                        onImportColors = { viewModel.importColors(it) }
                    )
                    Screen.Strings -> StringsEditorScreen(
                        strings = project.strings,
                        onStringUpdate = { key, value -> viewModel.updateString(key, value) },
                        onResetStrings = { viewModel.resetStrings() },
                        onImportStrings = { viewModel.importStrings(it) }
                    )
                    Screen.Images -> ImageManagerScreen(
                        images = project.images,
                        onSetImage = { path, uri -> viewModel.setImage(path, uri) },
                        onClearImage = { path -> viewModel.clearImage(path) },
                        onBatchImport = { mapping -> viewModel.batchImportImages(mapping) }
                    )
                    Screen.Export -> ExportScreen(
                        project = project,
                        onExport = { context ->
                            ThemeExporter.export(
                                context = context,
                                manifest = project.manifest,
                                colors = project.colors,
                                strings = project.strings,
                                images = project.images
                            )
                        },
                        onNavigateToAbout = { aboutVisible = true }
                    )
                    else -> Unit
                }
            }

            AnimatedVisibility(
                visible = aboutVisible,
                enter = fadeIn(tween(durationMillis = 250)),
                exit = fadeOut(tween(durationMillis = 250))
            ) {
                // 返回手势进行中：透明度跟手（1 → 0）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = 1f - backProgress
                        }
                ) {
                    AboutScreen(onBack = { aboutVisible = false })
                }
            }
        }
    }

    // 关于页打开时重置返回进度
    LaunchedEffect(aboutVisible) {
        if (aboutVisible) backProgress = 0f
    }

    // 预测性返回：返回手势进行中 about 浮层跟手淡出；手势提交关闭，滑回取消
    PredictiveBackHandler(enabled = aboutVisible) { progress ->
        var committed = false
        try {
            progress.collect { backEvent ->
                backProgress = backEvent.progress
            }
            committed = true
        } finally {
            if (committed) {
                aboutVisible = false
            } else {
                backProgress = 0f
            }
        }
    }
}
