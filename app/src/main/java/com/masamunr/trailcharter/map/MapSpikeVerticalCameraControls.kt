package com.masamunr.trailcharter.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Map camera controls deliberately float directly over the map without a panel behind the slider.
 *
 * The palette is selected from the resolved map backdrop rather than blindly following the Android
 * system theme. That keeps contrast correct if TrailCharter later offers a dark map while the app
 * theme remains light, or vice versa.
 */
@Composable
internal fun MapSpikeVerticalCameraControls(
    tiltSelected: Boolean,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onTiltSelected: () -> Unit,
    onZoomSelected: () -> Unit,
    mapBackdropIsDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = mapCameraControlPalette(mapBackdropIsDark)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        VerticalMapSlider(
            value = value,
            valueRange = valueRange,
            onValueChange = onValueChange,
            palette = palette,
        )

        Spacer(modifier = Modifier.height(8.dp))

        CameraModeToggle(
            tiltSelected = tiltSelected,
            onTiltSelected = onTiltSelected,
            onZoomSelected = onZoomSelected,
            palette = palette,
        )
    }
}

@Composable
private fun VerticalMapSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    palette: MapCameraControlPalette,
) {
    val span = (valueRange.endInclusive - valueRange.start).takeIf { it > 0f } ?: 1f
    val fraction = ((value - valueRange.start) / span).coerceIn(0f, 1f)

    Canvas(
        modifier = Modifier
            .width(52.dp)
            .height(238.dp)
            .pointerInput(valueRange.start, valueRange.endInclusive) {
                fun updateFromY(y: Float) {
                    val top = 22.dp.toPx()
                    val bottom = size.height - 22.dp.toPx()
                    val usable = (bottom - top).coerceAtLeast(1f)
                    val touchFraction = ((bottom - y) / usable).coerceIn(0f, 1f)
                    onValueChange(valueRange.start + touchFraction * span)
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    updateFromY(down.position.y)
                    drag(down.id) { change ->
                        updateFromY(change.position.y)
                        change.consume()
                    }
                }
            },
    ) {
        val centerX = size.width / 2f
        val thumbRadius = 18.dp.toPx()
        val top = thumbRadius + 4.dp.toPx()
        val bottom = size.height - thumbRadius - 4.dp.toPx()
        val usableHeight = bottom - top
        val thumbY = bottom - (usableHeight * fraction)
        val trackWidth = 5.dp.toPx()

        drawLine(
            color = palette.inactiveTrack,
            start = Offset(centerX, top),
            end = Offset(centerX, bottom),
            strokeWidth = trackWidth,
            cap = StrokeCap.Round,
        )

        drawLine(
            color = palette.activeTrack,
            start = Offset(centerX, thumbY),
            end = Offset(centerX, bottom),
            strokeWidth = trackWidth,
            cap = StrokeCap.Round,
        )

        val tickCount = 9
        repeat(tickCount) { index ->
            val tickFraction = index.toFloat() / (tickCount - 1).toFloat()
            val y = bottom - (usableHeight * tickFraction)
            drawCircle(
                color = if (tickFraction <= fraction) palette.tickOnActive else palette.tickOnInactive,
                radius = 2.6.dp.toPx(),
                center = Offset(centerX, y),
            )
        }

        drawCircle(
            color = palette.thumbOutline,
            radius = thumbRadius + 1.5.dp.toPx(),
            center = Offset(centerX, thumbY),
        )
        drawCircle(
            color = palette.thumb,
            radius = thumbRadius,
            center = Offset(centerX, thumbY),
        )
    }
}

@Composable
private fun CameraModeToggle(
    tiltSelected: Boolean,
    onTiltSelected: () -> Unit,
    onZoomSelected: () -> Unit,
    palette: MapCameraControlPalette,
) {
    Row(
        modifier = Modifier
            .background(palette.toggleContainer, RoundedCornerShape(18.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CameraModeSegment(
            label = "Tilt",
            selected = tiltSelected,
            onClick = onTiltSelected,
            palette = palette,
        )
        CameraModeSegment(
            label = "Zoom",
            selected = !tiltSelected,
            onClick = onZoomSelected,
            palette = palette,
        )
    }
}

@Composable
private fun CameraModeSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    palette: MapCameraControlPalette,
) {
    Text(
        text = label,
        modifier = Modifier
            .background(
                color = if (selected) palette.toggleSelected else Color.Transparent,
                shape = RoundedCornerShape(15.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        color = if (selected) palette.toggleSelectedText else palette.toggleText,
        style = MaterialTheme.typography.labelSmall,
    )
}

private data class MapCameraControlPalette(
    val activeTrack: Color,
    val inactiveTrack: Color,
    val tickOnActive: Color,
    val tickOnInactive: Color,
    val thumb: Color,
    val thumbOutline: Color,
    val toggleContainer: Color,
    val toggleSelected: Color,
    val toggleText: Color,
    val toggleSelectedText: Color,
)

private fun mapCameraControlPalette(mapBackdropIsDark: Boolean): MapCameraControlPalette =
    if (mapBackdropIsDark) {
        MapCameraControlPalette(
            activeTrack = Color(0xFFF4F2EC),
            inactiveTrack = Color(0xFF777777),
            tickOnActive = Color(0xFF464646),
            tickOnInactive = Color(0xFFEAE7DF),
            thumb = Color(0xFFF4F2EC),
            thumbOutline = Color(0xAA2D2D2D),
            toggleContainer = Color(0xB8333333),
            toggleSelected = Color(0xFFF4F2EC),
            toggleText = Color(0xFFF4F2EC),
            toggleSelectedText = Color(0xFF303030),
        )
    } else {
        MapCameraControlPalette(
            activeTrack = Color(0xFF414141),
            inactiveTrack = Color(0xFFB7B7B7),
            tickOnActive = Color(0xFFF6F6F2),
            tickOnInactive = Color(0xFF4B4B4B),
            thumb = Color(0xFF414141),
            thumbOutline = Color(0x99FFFFFF),
            toggleContainer = Color(0xD9F5F3EE),
            toggleSelected = Color(0xFF414141),
            toggleText = Color(0xFF414141),
            toggleSelectedText = Color(0xFFF8F7F2),
        )
    }
