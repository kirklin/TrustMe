package hk.kirk.trustme.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

@Composable
fun SimpleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TrustMe.colors
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "sw",
    )

    Canvas(
        modifier = modifier
            .size(width = 46.dp, height = 26.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onCheckedChange(!checked) },
    ) {
        val w = size.width
        val h = size.height
        val r = h / 2f

        // Track
        drawRoundRect(
            color = lerp(colors.switchOff, colors.switchOn, progress),
            size = Size(w, h),
            cornerRadius = CornerRadius(r),
        )

        // Thumb
        val thumbR = h * 0.38f
        val pad = (h - thumbR * 2) / 2f
        val cx = pad + thumbR + (w - 2 * pad - 2 * thumbR) * progress

        drawCircle(
            color = Color.White,
            radius = thumbR,
            center = Offset(cx, h / 2f),
        )
    }
}

private fun lerp(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = a.alpha + (b.alpha - a.alpha) * t,
)
