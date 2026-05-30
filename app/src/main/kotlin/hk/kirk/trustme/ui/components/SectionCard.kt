package hk.kirk.trustme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

/**
 * Section header label — muted color, aligned with card edges.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = title,
        style = TrustMe.type.label.copy(color = TrustMe.colors.textSecondary),
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 8.dp),
    )
}

/**
 * Card container — groups related settings items with rounded corners,
 * surface background, and a subtle border.
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shapes = TrustMe.shapes
    val colors = TrustMe.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(shapes.card))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(shapes.card),
            ),
        content = content,
    )
}

/**
 * Divider between items inside a card — indented on the left.
 */
@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .height(0.5.dp)
            .background(TrustMe.colors.divider),
    )
}

/**
 * Full-width divider.
 */
@Composable
fun Divider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(TrustMe.colors.divider),
    )
}
