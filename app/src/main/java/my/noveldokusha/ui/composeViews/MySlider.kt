package my.noveldokusha.ui.composeViews

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import my.noveldokusha.ui.theme.ColorAccent
import my.noveldokusha.ui.theme.InternalTheme
import my.noveldokusha.ui.theme.selectableMinHeight
import my.noveldokusha.utils.mix

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
) {
    MySlider(
        value = value,
        valueRange = valueRange,
        onValueChange = onValueChange,
        modifier = modifier,
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.contentColorFor(ColorAccent)
        )
    }
}

@Composable
fun MySlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        MySliderBase(
            range = valueRange,
            value = value,
            onValueChange = onValueChange,
        )
        overlayContent()
    }
}

@Composable
private fun MySliderBase(
    range: ClosedFloatingPointRange<Float>,
    value: Float,
    onValueChange: (Float) -> Unit,
    height: Dp = selectableMinHeight,
    backgroundColor: Color = ColorAccent.mix(MaterialTheme.colorScheme.primary, 0.5f),
    trackColor: Color = ColorAccent,
) {
    val currentValue by rememberUpdatedState(newValue = value)
    BoxWithConstraints {
        val currentDensity by rememberUpdatedState(newValue = LocalDensity.current)
        val heightPx = with(currentDensity) { height.toPx() }

        /**
         * Don't check for 1/0 Exception as is responsibility
         * of the user to guarantee the range is correct
         */
        val offsetPx by remember {
            derivedStateOf {
                val normalizedValue =
                    (currentValue - range.start) / (range.endInclusive - range.start)
                val sliderSizePx = constraints.maxWidth.toFloat() - heightPx
                normalizedValue * sliderSizePx + heightPx
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(CircleShape)
                .background(backgroundColor)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { deltaPx ->
                        val valueSize = (constraints.maxWidth.toFloat() - heightPx)
                        if (valueSize <= 0f) return@rememberDraggableState
                        val delta = deltaPx * (range.endInclusive - range.start) / valueSize

                        val newValue = (currentValue + delta).coerceIn(
                            minimumValue = range.start,
                            maximumValue = range.endInclusive
                        )
                        if (newValue != currentValue) {
                            onValueChange(newValue)
                        }
                    }
                )
        ) {
            Box(
                modifier = Modifier
                    .height(height)
                    .width(with(currentDensity) { offsetPx.toDp() })
                    .clip(RoundedCornerShape(bottomEndPercent = 50, topEndPercent = 50))
                    .background(trackColor)
            )
        }
    }
}

@Preview(heightDp = 120, widthDp = 500)
@Composable
fun MySliderBasePreview() {
    var value by remember { mutableStateOf(4f) }
    InternalTheme {
        BoxWithConstraints {
            MySliderBase(
                range = 0f..100f,
                value = value,
                onValueChange = {
                    value = it
                }
            )
        }
    }
}
