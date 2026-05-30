package hk.kirk.trustme.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import hk.kirk.trustme.ui.components.Divider
import hk.kirk.trustme.ui.components.InfoRow
import hk.kirk.trustme.ui.components.SectionHeader
import hk.kirk.trustme.ui.components.StatusRow
import hk.kirk.trustme.ui.components.SwitchRow
import hk.kirk.trustme.ui.theme.TrustMe

@Composable
fun SettingsScreen(
    isModuleActive: Boolean,
    versionName: String,
    prefs: PrefAccessor,
) {
    var showLogs by remember { mutableStateOf(false) }

    if (showLogs) {
        LogScreen(onBack = { showLogs = false })
    } else {
        SettingsContent(
            isModuleActive = isModuleActive,
            versionName = versionName,
            prefs = prefs,
            onShowLogs = { showLogs = true },
        )
    }
}

@Composable
private fun SettingsContent(
    isModuleActive: Boolean,
    versionName: String,
    prefs: PrefAccessor,
    onShowLogs: () -> Unit,
) {
    val colors = TrustMe.colors
    val type = TrustMe.type
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
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
                text = "TrustMe",
                style = type.titleLarge.copy(color = colors.textPrimary),
            )
            Spacer(modifier = Modifier.weight(1f))
            BasicText(
                text = "v$versionName",
                style = type.bodySmall.copy(color = colors.textSecondary),
            )
        }

        Divider()

        // ── 状态 ──
        StatusRow(isActive = isModuleActive)

        Divider()

        // ── 常规设置 ──
        SectionHeader(title = "常规设置")
        SwitchRow(
            title = "全局开关",
            subtitle = "启用/禁用所有 SSL Pinning 绕过",
            checked = prefs.getBoolean("enabled", true),
            onCheckedChange = { prefs.putBoolean("enabled", it) },
        )
        Divider(modifier = Modifier.padding(start = 20.dp))
        SwitchRow(
            title = "Hook 日志",
            subtitle = "记录每次被绕过的 SSL 验证调用",
            checked = prefs.getBoolean("logging", true),
            onCheckedChange = { prefs.putBoolean("logging", it) },
        )
        Divider(modifier = Modifier.padding(start = 20.dp))
        InfoRow(
            title = "查看日志",
            subtitle = "查看 Hook 触发记录",
            showArrow = true,
            onClick = onShowLogs,
        )

        // ── Hook 模块 ──
        SectionHeader(title = "Hook 模块")
        HookItem(prefs, "hook_sslcontext", "SSLContext.init", "替换 TrustManager 为信任一切的实现")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_trustmanager", "TrustManager", "绕过证书验证方法")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_conscrypt", "Conscrypt TrustManagerImpl", "动态反射绕过 checkTrustedRecursive / verifyChain")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_okhttp", "OkHttp 2.x / 3.x / 4.x+", "绕过 CertificatePinner 和 OkHostnameVerifier")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_urlconnection", "HttpsURLConnection", "无效化 HostnameVerifier 和 SSLSocketFactory")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_webview", "WebView", "自动处理 WebView SSL 错误")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_apache", "Apache HTTP Client", "替换 DefaultHttpClient 和 SSLSocketFactory")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_nsc", "Network Security Config", "绕过 Android 7+ 网络安全配置")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_conscryptsocket", "Conscrypt Socket", "绕过底层 Socket 证书链验证")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_pinmanager", "Conscrypt PinManager", "绕过 Conscrypt 内置 Pin 管理")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_cronet", "Cronet 引擎", "绕过 Chromium Cronet SSL 配置")
        Divider(modifier = Modifier.padding(start = 20.dp))
        HookItem(prefs, "hook_thirdparty", "第三方库", "xutils / httpclientandroidlib")

        // ── 关于 ──
        SectionHeader(title = "关于")
        InfoRow(title = "作者", subtitle = "Kirk Lin")
        Divider(modifier = Modifier.padding(start = 20.dp))
        InfoRow(
            title = "GitHub",
            subtitle = "github.com/kirklin",
            showArrow = true,
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/kirklin"))
                )
            },
        )

        Spacer(modifier = Modifier.padding(bottom = 32.dp))
    }
}

@Composable
private fun HookItem(prefs: PrefAccessor, key: String, title: String, subtitle: String) {
    SwitchRow(
        title = title,
        subtitle = subtitle,
        checked = prefs.getBoolean(key, true),
        onCheckedChange = { prefs.putBoolean(key, it) },
        isMono = true,
    )
}

class PrefAccessor(
    private val prefs: android.content.SharedPreferences,
    private val onPrefsChanged: (() -> Unit)? = null,
) {
    private val states = mutableMapOf<String, androidx.compose.runtime.MutableState<Boolean>>()

    fun getBoolean(key: String, default: Boolean): Boolean =
        getOrCreate(key, default).value

    fun putBoolean(key: String, value: Boolean) {
        getOrCreate(key, prefs.getBoolean(key, true)).value = value
        prefs.edit().putBoolean(key, value).commit()  // 用 commit() 确保同步写入
        onPrefsChanged?.invoke()  // 写入后修复文件权限
    }

    private fun getOrCreate(key: String, default: Boolean) =
        states.getOrPut(key) { mutableStateOf(prefs.getBoolean(key, default)) }
}
