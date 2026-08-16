package com.johnny.wekit.theme.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.johnny.wekit.theme.util.ThemeExporter
import com.johnny.wekit.theme.viewmodel.ThemeViewModel

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
    val navController = rememberNavController()
    val project by viewModel.project.collectAsState()

    val screens = listOf(
        Screen.Theme,
        Screen.Colors,
        Screen.Strings,
        Screen.Images,
        Screen.Export
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                screens.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
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
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Theme.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            composable(Screen.Theme.route) {
                ThemeEditorScreen(
                    manifest = project.manifest,
                    onManifestUpdate = { viewModel.updateManifest(it) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
            composable(Screen.Colors.route) {
                ColorsEditorScreen(
                    colors = project.colors,
                    onColorUpdate = { key, value -> viewModel.updateColor(key, value) },
                    onResetColors = { viewModel.resetColors() },
                    onImportColors = { viewModel.importColors(it) }
                )
            }
            composable(Screen.Strings.route) {
                StringsEditorScreen(
                    strings = project.strings,
                    onStringUpdate = { key, value -> viewModel.updateString(key, value) },
                    onResetStrings = { viewModel.resetStrings() },
                    onImportStrings = { viewModel.importStrings(it) }
                )
            }
            composable(Screen.Images.route) {
                ImageManagerScreen(
                    images = project.images,
                    onSetImage = { path, uri -> viewModel.setImage(path, uri) },
                    onClearImage = { path -> viewModel.clearImage(path) },
                    onBatchImport = { mapping -> viewModel.batchImportImages(mapping) }
                )
            }
            composable(Screen.Export.route) {
                ExportScreen(
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
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
