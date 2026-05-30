package hk.kirk.trustme.utils

import android.content.Context
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

/**
 * 日志文件读取器 — 供 App UI 端读取 Hook 日志
 *
 * 支持两种读取路径:
 * 1. Context 方式 (推荐) — 通过 App 自身的 filesDir 定位
 * 2. 硬编码路径 — 作为 fallback
 */
object LogFileReader {

    private const val LOG_DIR = "/data/data/hk.kirk.trustme/shared_prefs"
    private const val LOG_FILE = "trustme_logs.jsonl"

    data class LogEntry(
        val timestamp: Long,
        val app: String,
        val hook: String,
        val message: String,
    )

    private fun getLogFile(context: Context?): File {
        // 优先使用 Context 定位（确保路径正确）
        if (context != null) {
            try {
                val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                return File(prefsDir, LOG_FILE)
            } catch (_: Throwable) {}
        }
        return File(LOG_DIR, LOG_FILE)
    }

    /** 读取全部日志，最新的在前 */
    fun readAll(context: Context? = null): List<LogEntry> {
        return try {
            val file = getLogFile(context)
            if (!file.exists()) return emptyList()

            BufferedReader(FileReader(file)).useLines { lines ->
                lines
                    .filter { it.isNotBlank() }
                    .mapNotNull { parseEntry(it) }
                    .toList()
                    .reversed()
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    /** 清空日志文件 */
    fun clear(context: Context? = null) {
        try {
            getLogFile(context).delete()
        } catch (_: Throwable) {}
    }

    private fun parseEntry(line: String): LogEntry? {
        return try {
            val json = JSONObject(line)
            LogEntry(
                timestamp = json.getLong("ts"),
                app = json.getString("app"),
                hook = json.getString("hook"),
                message = json.getString("msg"),
            )
        } catch (_: Throwable) {
            null
        }
    }
}
