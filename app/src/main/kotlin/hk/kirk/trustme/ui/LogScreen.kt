package hk.kirk.trustme.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.components.Divider
import hk.kirk.trustme.ui.theme.TrustMe
import hk.kirk.trustme.utils.LogFileReader

@Composable
fun LogScreen(onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onBack)

    val colors = TrustMe.colors
    val type = TrustMe.type
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
        // ── 顶栏 ──
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(logs.size) { index ->
                    LogEntryRow(logs[index])
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: LogFileReader.LogEntry) {
    val colors = TrustMe.colors
    val type = TrustMe.type

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        // 第一行: 包名 + hook 名
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            BasicText(
                text = entry.app,
                style = type.mono.copy(color = colors.textPrimary),
                modifier = Modifier.weight(1f).padding(end = 12.dp),
            )
            BasicText(
                text = entry.hook,
                style = type.bodySmall.copy(color = colors.textSecondary),
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 第二行: 消息
        BasicText(
            text = entry.message,
            style = type.bodySmall.copy(color = colors.textSecondary),
        )

        Spacer(modifier = Modifier.height(2.dp))

        // 第三行: 相对时间
        BasicText(
            text = relativeTime(entry.timestamp),
            style = type.bodySmall.copy(color = Color(0xFF333639)),
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
