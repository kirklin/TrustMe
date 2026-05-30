package hk.kirk.trustme.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

@Composable
fun InfoRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showArrow: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = TrustMe.colors
    val type = TrustMe.type

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            BasicText(
                text = title,
                style = type.body.copy(color = colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(2.dp))
            BasicText(
                text = subtitle,
                style = type.bodySmall.copy(color = colors.textSecondary),
            )
        }
        if (showArrow) {
            ChevronRight(
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Canvas-drawn chevron right icon — clean and precise.
 */
@Composable
private fun ChevronRight(modifier: Modifier = Modifier) {
    val color = TrustMe.colors.textTertiary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 1.5.dp.toPx()
        val padX = w * 0.3f
        val padY = h * 0.2f

        drawLine(
            color = color,
            start = Offset(padX, padY),
            end = Offset(w - padX, h / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(w - padX, h / 2f),
            end = Offset(padX, h - padY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
