package com.johnny.wekit.theme.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * 应用栏操作项：裸图标按钮（无容器图形），长按显示操作名称提示。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBarAction(
    tooltip: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { Text(tooltip) },
        state = tooltipState
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        tryAwaitRelease()
                        tooltipState.dismiss()
                    },
                    onLongPress = { scope.launch { tooltipState.show() } }
                )
            }
        ) {
            Icon(icon, contentDescription = tooltip)
        }
    }
}
