package hk.kirk.trustme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

/**
 * Status card — surface-colored card with a thin left accent bar
 * indicating module activation state. Visually consistent with SectionCard.
 */
@Composable
fun StatusCard(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = TrustMe.colors
    val type = TrustMe.type
    val shapes = TrustMe.shapes

    val accentColor = if (isActive) colors.green else colors.red

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(shapes.card))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(shapes.card),
            ),
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accentColor),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BasicText(
                text = if (isActive) "模块已激活" else "模块未激活",
                style = type.body.copy(color = colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(2.dp))
            BasicText(
                text = if (isActive)
                    "LSPosed 框架已加载，Hook 正常运行"
                else
                    "请在 LSPosed Manager 中启用本模块",
                style = type.bodySmall.copy(color = colors.textSecondary),
            )
        }
    }
}
