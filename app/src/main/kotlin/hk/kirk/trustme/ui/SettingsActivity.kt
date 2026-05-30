package hk.kirk.trustme.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import hk.kirk.trustme.ui.theme.TrustMeTheme
import java.io.File

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

        val prefs = try {
            getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE)
        } catch (_: Exception) {
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        // 确保 prefs 文件和目录对其他进程可读
        makePrefsWorldReadable()

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

        val prefAccessor = PrefAccessor(prefs, ::makePrefsWorldReadable)

        setContent {
            TrustMeTheme {
                SettingsScreen(
                    isModuleActive = isModuleActive,
                    versionName = versionName,
                    prefs = prefAccessor,
                )
            }
        }
    }

    /**
     * 手动设置 prefs 文件及其父目录为 world-readable
     * 这是 LSPosed 模块在新版 Android 上的标准 workaround
     */
    private fun makePrefsWorldReadable() {
        try {
            val prefsDir = File(applicationInfo.dataDir, "shared_prefs")
            val prefsFile = File(prefsDir, "$PREFS_NAME.xml")

            // 设置目录权限：owner rwx, group rx, other rx (755)
            prefsDir.setReadable(true, false)
            prefsDir.setExecutable(true, false)

            // 设置文件权限：owner rw, group r, other r (644)
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false)
            }

            // 同时确保 data 目录本身可访问
            val dataDir = File(applicationInfo.dataDir)
            dataDir.setExecutable(true, false)
        } catch (e: Exception) {
            android.util.Log.e("TrustMe", "Failed to set prefs permissions", e)
        }
    }
}

