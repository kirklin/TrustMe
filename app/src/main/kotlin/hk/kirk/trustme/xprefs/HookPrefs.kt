package hk.kirk.trustme.xprefs

/**
 * Hook 端全局配置快捷入口
 *
 * 简化 Hook 回调中的开关检查：
 * ```kotlin
 * if (!HookPrefs.isHookActive("okhttp")) return
 * ```
 */
object HookPrefs {
    private const val MY_PACKAGE = "hk.kirk.trustme"
    private const val PREFS_NAME = "trustme_prefs"

    private val prefs = XposedPrefs.hook(MY_PACKAGE, PREFS_NAME)

    /** prefs 文件是否可读 */
    val isAvailable: Boolean get() = prefs.isAvailable

    /** 全局开关是否开启 */
    fun isEnabled(): Boolean = prefs.getBoolean("enabled", true)

    /** 日志开关是否开启 */
    fun isLoggingEnabled(): Boolean = prefs.getBoolean("logging", true)

    /** 指定 Hook 模块是否处于激活状态（全局开关 && 模块开关） */
    fun isHookActive(hookName: String): Boolean {
        return prefs.getBoolean("enabled", true) && prefs.getBoolean("hook_$hookName", true)
    }

    /** 重新加载配置 */
    fun reload() = prefs.reload()
}
