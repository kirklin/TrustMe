package hk.kirk.trustme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.components.Divider
import hk.kirk.trustme.ui.theme.TrustMe
import hk.kirk.trustme.utils.LogFileReader

@Composable
fun LogScreen(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    val colors = TrustMe.colors
    val type = TrustMe.type
    val shapes = TrustMe.shapes
    val context = androidx.compose.ui.platform.LocalContext.current
    val logs = remember { mutableStateListOf<LogFileReader.LogEntry>() }

    LaunchedEffect(Unit) {
        logs.clear()
        logs.addAll(LogFileReader.readAll(context))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                text = "←",
                style = type.titleLarge.copy(color = colors.textPrimary),
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(end = 16.dp),
            )
            BasicText(
                text = "Hook 日志",
                style = type.titleLarge.copy(color = colors.textPrimary),
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicText(
                text = "刷新",
                style = type.body.copy(color = colors.accent),
                modifier = Modifier
                    .clickable {
                        logs.clear()
                        logs.addAll(LogFileReader.readAll(context))
                    }
                    .padding(horizontal = 8.dp),
            )
            BasicText(
                text = "清除",
                style = type.body.copy(color = colors.red),
                modifier = Modifier
                    .clickable {
                        LogFileReader.clear(context)
                        logs.clear()
                    }
                    .padding(start = 8.dp),
            )
        }

        Divider()

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "暂无日志",
                    style = type.body.copy(color = colors.textSecondary),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(logs.size) { index ->
                    LogEntryCard(logs[index])
                }
            }
        }
    }
}

@Composable
private fun LogEntryCard(entry: LogFileReader.LogEntry) {
    val colors = TrustMe.colors
    val type = TrustMe.type
    val shapes = TrustMe.shapes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(shapes.inner))
            .background(colors.surface)
            .border(
                width = 1.dp,
                color = colors.border,
                shape = RoundedCornerShape(shapes.inner),
            )
            .padding(14.dp),
    ) {
        // App name + hook tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BasicText(
                text = entry.app,
                style = type.mono.copy(color = colors.textPrimary),
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            BasicText(
                text = entry.hook,
                style = type.bodySmall.copy(color = colors.textSecondary),
                modifier = Modifier
                    .clip(RoundedCornerShape(shapes.chip))
                    .background(colors.border)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Message
        BasicText(
            text = entry.message,
            style = type.bodySmall.copy(color = colors.textSecondary),
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Relative time
        BasicText(
            text = relativeTime(entry.timestamp),
            style = type.bodySmall.copy(color = colors.textSecondary),
        )
    }
}

private fun relativeTime(timestamp: Long): String {
    val diff = (System.currentTimeMillis() - timestamp) / 1000
    return when {
        diff < 60 -> "刚刚"
        diff < 3600 -> "${diff / 60}分钟前"
        diff < 86400 -> "${diff / 3600}小时前"
        else -> "${diff / 86400}天前"
    }
}
