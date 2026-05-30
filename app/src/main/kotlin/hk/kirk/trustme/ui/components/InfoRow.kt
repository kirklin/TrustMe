package hk.kirk.trustme.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
            BasicText(
                text = "›",
                style = type.body.copy(color = colors.textSecondary, fontSize = 20.sp),
            )
        }
    }
}
