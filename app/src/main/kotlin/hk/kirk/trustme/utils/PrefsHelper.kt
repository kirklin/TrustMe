package hk.kirk.trustme.utils

import de.robv.android.xposed.XSharedPreferences
import java.io.File

/**
 * SharedPreferences 辅助工具
 *
 * 在 Hook 端（被 Hook 的 App 进程）通过 XSharedPreferences 读取设置界面的配置。
 */
object PrefsHelper {

    private const val PACKAGE_NAME = "hk.kirk.trustme"
    private const val PREFS_NAME = "trustme_prefs"

    private var xPrefs: XSharedPreferences? = null
    private var prefsReadable = false

    fun init() {
        try {
            xPrefs = XSharedPreferences(PACKAGE_NAME, PREFS_NAME)
            xPrefs?.makeWorldReadable()

            // 验证文件是否可读
            val file = xPrefs?.file
            prefsReadable = file != null && file.exists() && file.canRead()

            if (prefsReadable) {
                Logger.i("XSharedPreferences 初始化成功: ${file?.absolutePath}")
            } else {
                Logger.e("XSharedPreferences 文件不可读: ${file?.absolutePath}, exists=${file?.exists()}, canRead=${file?.canRead()}")
            }
        } catch (e: Exception) {
            Logger.e("Failed to init XSharedPreferences", e)
            prefsReadable = false
        }
    }

    fun reload() {
        try {
            xPrefs?.reload()

            // 重新检查可读性
            val file = xPrefs?.file
            prefsReadable = file != null && file.exists() && file.canRead()
        } catch (e: Exception) {
            Logger.e("Failed to reload prefs", e)
            prefsReadable = false
        }
    }

    /** Prefs 文件是否可读 */
    fun canReadPrefs(): Boolean = prefsReadable

    /** 全局开关 — 如果 prefs 不可读，默认启用（保持向后兼容） */
    fun isEnabled(): Boolean {
        if (!prefsReadable) {
            Logger.d("Prefs 不可读，全局开关默认启用")
            return true
        }
        return xPrefs?.getBoolean("enabled", true) ?: true
    }

    /** 日志开关 */
    fun isLoggingEnabled(): Boolean {
        if (!prefsReadable) return true
        return xPrefs?.getBoolean("logging", true) ?: true
    }

    /** 各 Hook 模块开关 */
    fun isHookEnabled(hookName: String): Boolean {
        if (!prefsReadable) {
            Logger.d("Prefs 不可读，Hook $hookName 默认启用")
            return true
        }
        return xPrefs?.getBoolean("hook_$hookName", true) ?: true
    }
}

