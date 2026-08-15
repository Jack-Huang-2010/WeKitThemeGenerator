package com.johnny.wekit.theme.ui

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple HSV color picker dialog.
 * Returns color in RRGGBB or AARRGGBB format (no # prefix).
 */
@Composable
fun ColorPickerDialog(
    initialColor: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialColorInt = remember(initialColor) { parseHexColor(initialColor) }
    val initialAlpha = remember(initialColorInt) { ((initialColorInt shr 24) and 0xFF) / 255f }

    val hsvArray = remember(initialColorInt) {
        FloatArray(3).also {
            AndroidColor.RGBToHSV(
                (initialColorInt shr 16) and 0xFF,
                (initialColorInt shr 8) and 0xFF,
                initialColorInt and 0xFF,
                it
            )
        }
    }

    var hue by remember { mutableFloatStateOf(hsvArray[0]) }
    var saturation by remember { mutableFloatStateOf(hsvArray[1]) }
    var brightness by remember { mutableFloatStateOf(hsvArray[2]) }
    var alpha by remember { mutableFloatStateOf(initialAlpha) }
    var hexInput by remember { mutableStateOf(initialColor) }

    fun updateHexFromHSV() {
        val colorInt = AndroidColor.HSVToColor(
            (alpha * 255).toInt(),
            floatArrayOf(hue, saturation, brightness)
        )
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        hexInput = if (alpha < 1f) {
            String.format("%02X%02X%02X%02X", (alpha * 255).toInt(), r, g, b)
        } else {
            String.format("%02X%02X%02X", r, g, b)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择颜色") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // SV Panel
                SVPanel(
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    onSVChange = { s, v ->
                        saturation = s
                        brightness = v
                        updateHexFromHSV()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hue Slider
                Text("色相", fontSize = 12.sp)
                HueSlider(
                    hue = hue,
                    onHueChange = { newHue ->
                        hue = newHue
                        updateHexFromHSV()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Alpha Slider
                Text("透明度", fontSize = 12.sp)
                AlphaSlider(
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    alpha = alpha,
                    onAlphaChange = { newAlpha ->
                        alpha = newAlpha
                        updateHexFromHSV()
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color preview + hex input
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val previewColor = Color(
                        AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness))
                    )
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = previewColor.copy(alpha = alpha),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = hexInput,
                        onValueChange = { newHex ->
                            hexInput = newHex
                            try {
                                val parsed = parseHexColor(newHex)
                                val newAlpha = ((parsed shr 24) and 0xFF) / 255f
                                val newHsv = FloatArray(3)
                                AndroidColor.RGBToHSV(
                                    (parsed shr 16) and 0xFF,
                                    (parsed shr 8) and 0xFF,
                                    parsed and 0xFF,
                                    newHsv
                                )
                                hue = newHsv[0]
                                saturation = newHsv[1]
                                brightness = newHsv[2]
                                alpha = newAlpha
                            } catch (_: Exception) {
                                // Invalid hex input, ignore
                            }
                        },
                        label = { Text("颜色值 (RRGGBB)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hexInput) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun SVPanel(
    hue: Float,
    saturation: Float,
    brightness: Float,
    onSVChange: (Float, Float) -> Unit
) {
    val hueColor = remember(hue) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        onSVChange(
                            (down.position.x / size.width).coerceIn(0f, 1f),
                            (1f - down.position.y / size.height).coerceIn(0f, 1f)
                        )
                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                onSVChange(
                                    (change.position.x / size.width).coerceIn(0f, 1f),
                                    (1f - change.position.y / size.height).coerceIn(0f, 1f)
                                )
                                change.consume()
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            // White to Hue gradient (left to right)
            drawRect(
                Brush.horizontalGradient(
                    colors = listOf(Color.White, hueColor)
                )
            )
            // Transparent to Black gradient (top to bottom)
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black)
                )
            )
            // Marker
            val markerX = saturation * size.width
            val markerY = (1f - brightness) * size.height
            drawCircle(
                color = Color.White,
                radius = 10f,
                center = Offset(markerX, markerY),
                style = Fill
            )
            drawCircle(
                color = Color.Black,
                radius = 10f,
                center = Offset(markerX, markerY),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
private fun HueSlider(
    hue: Float,
    onHueChange: (Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onHueChange((down.position.x / size.width * 360f).coerceIn(0f, 360f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            onHueChange((change.position.x / size.width * 360f).coerceIn(0f, 360f))
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // Rainbow gradient
        val rainbowColors = listOf(
            Color(0xFFFF0000), // 0°
            Color(0xFFFF00FF), // 60°
            Color(0xFF0000FF), // 120°
            Color(0xFF00FFFF), // 180°
            Color(0xFF00FF00), // 240°
            Color(0xFFFFFF00), // 300°
            Color(0xFFFF0000)  // 360°
        )
        drawRect(Brush.horizontalGradient(colors = rainbowColors))

        // Thumb
        val thumbX = (hue / 360f) * size.width
        val thumbY = size.height / 2f
        drawCircle(Color.White, radius = size.height / 2f, center = Offset(thumbX, thumbY))
        drawCircle(
            Color.Black,
            radius = size.height / 2f,
            center = Offset(thumbX, thumbY),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun AlphaSlider(
    hue: Float,
    saturation: Float,
    brightness: Float,
    alpha: Float,
    onAlphaChange: (Float) -> Unit
) {
    val currentColor = remember(hue, saturation, brightness) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, saturation, brightness)))
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onAlphaChange((down.position.x / size.width).coerceIn(0f, 1f))
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            onAlphaChange((change.position.x / size.width).coerceIn(0f, 1f))
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // Checkerboard background
        val checkerSize = 6f
        val cols = (size.width / checkerSize).toInt() + 1
        val rows = (size.height / checkerSize).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val isLight = (row + col) % 2 == 0
                drawRect(
                    color = if (isLight) Color.LightGray else Color.White,
                    topLeft = Offset(col * checkerSize, row * checkerSize),
                    size = Size(checkerSize, checkerSize)
                )
            }
        }

        // Transparent to color gradient
        drawRect(
            Brush.horizontalGradient(
                colors = listOf(Color.Transparent, currentColor)
            )
        )

        // Thumb
        val thumbX = alpha * size.width
        val thumbY = size.height / 2f
        drawCircle(Color.White, radius = size.height / 2f, center = Offset(thumbX, thumbY))
        drawCircle(
            Color.Black,
            radius = size.height / 2f,
            center = Offset(thumbX, thumbY),
            style = Stroke(width = 2f)
        )
    }
}

/**
 * Parse a hex color string (RRGGBB or AARRGGBB) to an ARGB Int.
 */
private fun parseHexColor(hex: String): Int {
    val clean = hex.removePrefix("#").trim()
    return when (clean.length) {
        6 -> "FF$clean".toLong(16).toInt()
        8 -> clean.toLong(16).toInt()
        else -> 0xFF000000.toInt()
    }
}
