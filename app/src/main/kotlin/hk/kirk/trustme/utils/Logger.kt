package hk.kirk.trustme.utils

import de.robv.android.xposed.XposedBridge

/**
 * 日志工具 — 封装 XposedBridge.log + 文件日志
 */
object Logger {

    private const val TAG = "TrustMe"
    private var loggingEnabled = true

    /** 当前正在执行的 Hook 模块名称，由 hookSafely 设置 */
    var currentHook: String = "general"

    fun setLoggingEnabled(enabled: Boolean) {
        loggingEnabled = enabled
    }

    /** 始终输出的日志（模块加载、关键状态） */
    fun i(message: String) {
        XposedBridge.log("[$TAG] $message")
    }

    /** 仅在日志开关打开时输出（Hook 触发），自动使用 currentHook */
    fun d(message: String) {
        if (loggingEnabled) {
            XposedBridge.log("[$TAG] $message")
            try {
                LogFileWriter.write(currentHook, message)
            } catch (_: Throwable) {}
        }
    }

    /** 错误日志（始终输出） */
    fun e(message: String, throwable: Throwable? = null) {
        XposedBridge.log("[$TAG] ERROR: $message")
        throwable?.let { XposedBridge.log(it) }
    }
}
