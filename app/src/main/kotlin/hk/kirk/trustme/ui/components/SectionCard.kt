package hk.kirk.trustme.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.theme.TrustMe

/**
 * Section 标题 — X 风格，小字灰色
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    BasicText(
        text = title,
        style = TrustMe.type.sectionHeader.copy(color = TrustMe.colors.textSecondary),
        modifier = modifier
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 8.dp),
    )
}

/**
 * 分隔线
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
