package hk.kirk.trustme.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import hk.kirk.trustme.ui.theme.TrustMeTheme
import hk.kirk.trustme.xprefs.XposedPrefs

/**
 * 设置界面 — Jetpack Compose 实现
 */
class SettingsActivity : ComponentActivity() {

    companion object {
        private const val PREFS_NAME = "trustme_prefs"

        /** 模块激活状态 — MainHook 在 onCreate 前通过 Xposed 设为 true */
        @JvmStatic
        var isModuleActive = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge 纯黑
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT,
            ),
        )

        super.onCreate(savedInstanceState)

        // 使用 XposedPrefs 桥接 — 自动处理 MODE_WORLD_READABLE / fallback / 权限
        val prefs = XposedPrefs.module(this, PREFS_NAME)

        // 确保日志文件对 Hook 进程可读可写
        prefs.makeFileWorldWritable("trustme_logs.jsonl")

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

        setContent {
            TrustMeTheme {
                SettingsScreen(
                    isModuleActive = isModuleActive,
                    versionName = versionName,
                    prefs = prefs,
                )
            }
        }
    }
}
