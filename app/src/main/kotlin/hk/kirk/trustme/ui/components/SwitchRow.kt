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
import hk.kirk.trustme.ui.theme.TrustMe

@Composable
fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isMono: Boolean = false,
) {
    val colors = TrustMe.colors
    val type = TrustMe.type

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            BasicText(
                text = title,
                style = (if (isMono) type.mono else type.body).copy(color = colors.textPrimary),
            )
            Spacer(modifier = Modifier.height(2.dp))
            BasicText(
                text = subtitle,
                style = type.bodySmall.copy(color = colors.textSecondary),
            )
        }
        SimpleSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
