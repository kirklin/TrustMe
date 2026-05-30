package hk.kirk.trustme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

/**
 * 状态行 — 圆点 + 标题 + 副标题，像 X 的简洁风格
 */
@Composable
fun StatusRow(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = TrustMe.colors
    val type = TrustMe.type

    val dotColor = if (isActive) colors.green else colors.red
    val titleColor = if (isActive) colors.green else colors.red

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            BasicText(
                text = if (isActive) "模块已激活" else "模块未激活",
                style = type.body.copy(color = titleColor),
            )
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
