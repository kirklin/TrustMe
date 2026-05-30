package hk.kirk.trustme.xprefs

import android.content.Context
import android.content.SharedPreferences
import de.robv.android.xposed.XSharedPreferences
import java.io.File

/**
 * Xposed 模块配置存储桥
 *
 * 透明地解决模块 UI 端与 Hook（宿主进程）端之间 SharedPreferences 的跨进程读取问题。
 *
 * 用法：
 * - 模块 UI 端: `XposedPrefs.module(context, "my_prefs")`  → 读/写
 * - Hook 端:    `XposedPrefs.hook("com.example.module", "my_prefs")` → 只读
 *
 * 设计亮点:
 * - 零框架依赖，单文件可复制到任意项目
 * - sealed class 编译期区分环境
 * - 自动检测 New XSharedPreferences (LSPosed)，跳过不必要的文件权限修改
 */
sealed class XposedPrefs {

    abstract fun getBoolean(key: String, default: Boolean = false): Boolean
    abstract fun getString(key: String, default: String = ""): String
    abstract fun getInt(key: String, default: Int = 0): Int
    abstract fun getLong(key: String, default: Long = 0L): Long
    abstract fun getFloat(key: String, default: Float = 0f): Float
    abstract fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>
    abstract fun contains(key: String): Boolean
    abstract fun all(): Map<String, *>

    /** 当前存储是否可用 */
    abstract val isAvailable: Boolean

    companion object {
        /**
         * 创建模块 UI 端实例（读写）
         *
         * @param context Activity/Application 的 Context
         * @param prefsName SharedPreferences 文件名（不含 .xml）
         */
        fun module(context: Context, prefsName: String): ModuleSide =
            ModuleSide(context, prefsName)

        /**
         * 创建 Hook（宿主进程）端实例（只读）
         *
         * @param modulePackageName 模块的包名
         * @param prefsName SharedPreferences 文件名（不含 .xml）
         */
        fun hook(modulePackageName: String, prefsName: String): HookSide =
            HookSide(modulePackageName, prefsName)
    }

    // ========================================================================
    // 模块 UI 端 — 读写
    // ========================================================================

    class ModuleSide internal constructor(
        private val context: Context,
        private val prefsName: String,
    ) : XposedPrefs() {

        /** 是否处于 New XSharedPreferences 模式（LSPosed 接管了跨进程通信） */
        var isNewXShareMode: Boolean = false
            private set

        private val sp: SharedPreferences = try {
            @Suppress("DEPRECATION", "WorldReadableFiles")
            context.getSharedPreferences(prefsName, Context.MODE_WORLD_READABLE).also {
                isNewXShareMode = true
            }
        } catch (_: SecurityException) {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE).also {
                isNewXShareMode = false
                // 传统模式：手动修改文件权限
                makeWorldReadable()
            }
        }

        override val isAvailable: Boolean get() = true

        override fun getBoolean(key: String, default: Boolean) = sp.getBoolean(key, default)
        override fun getString(key: String, default: String) = sp.getString(key, default) ?: default
        override fun getInt(key: String, default: Int) = sp.getInt(key, default)
        override fun getLong(key: String, default: Long) = sp.getLong(key, default)
        override fun getFloat(key: String, default: Float) = sp.getFloat(key, default)
        override fun getStringSet(key: String, default: Set<String>) = sp.getStringSet(key, default) ?: default
        override fun contains(key: String) = sp.contains(key)
        override fun all(): Map<String, *> = sp.all

        /** 写入 Boolean 值 */
        fun putBoolean(key: String, value: Boolean) = edit { putBoolean(key, value) }

        /** 写入 String 值 */
        fun putString(key: String, value: String) = edit { putString(key, value) }

        /** 写入 Int 值 */
        fun putInt(key: String, value: Int) = edit { putInt(key, value) }

        /** 写入 Long 值 */
        fun putLong(key: String, value: Long) = edit { putLong(key, value) }

        /** 写入 Float 值 */
        fun putFloat(key: String, value: Float) = edit { putFloat(key, value) }

        /** 写入 StringSet 值 */
        fun putStringSet(key: String, value: Set<String>) = edit { putStringSet(key, value) }

        /** 移除指定键 */
        fun remove(key: String) = edit { remove(key) }

        /** 清空所有数据 */
        fun clear() = edit { clear() }

        /** 编辑并提交，自动处理权限 */
        private inline fun edit(crossinline block: SharedPreferences.Editor.() -> Unit) {
            sp.edit().apply { block() }.commit()
            if (!isNewXShareMode) makeWorldReadable()
        }

        /**
         * 确保 prefs 文件和父目录对其他进程可读
         *
         * 仅在传统模式（非 New XSharedPreferences）下调用
         */
        fun makeWorldReadable() {
            runCatching {
                val dataDir = File(context.applicationInfo.dataDir)
                dataDir.setExecutable(true, false)

                val prefsDir = File(dataDir, "shared_prefs")
                prefsDir.setReadable(true, false)
                prefsDir.setExecutable(true, false)

                val prefsFile = File(prefsDir, "$prefsName.xml")
                if (prefsFile.exists()) {
                    prefsFile.setReadable(true, false)
                }
            }
        }

        /**
         * 确保指定文件对其他进程可读可写（用于日志文件等）
         */
        fun makeFileWorldWritable(fileName: String) {
            runCatching {
                val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
                prefsDir.setWritable(true, false)

                val file = File(prefsDir, fileName)
                if (!file.exists()) file.createNewFile()
                file.setReadable(true, false)
                file.setWritable(true, false)
            }
        }

        /** 获取 shared_prefs 目录路径 */
        val prefsDir: File
            get() = File(context.applicationInfo.dataDir, "shared_prefs")
    }

    // ========================================================================
    // Hook（宿主进程）端 — 只读
    // ========================================================================

    class HookSide internal constructor(
        private val modulePackageName: String,
        private val prefsName: String,
    ) : XposedPrefs() {

        private val xsp: XSharedPreferences? = try {
            XSharedPreferences(modulePackageName, prefsName).apply {
                makeWorldReadable()
            }
        } catch (e: Throwable) {
            null
        }

        override val isAvailable: Boolean
            get() = xsp?.file?.let { it.exists() && it.canRead() } ?: false

        /** 重新加载配置（从磁盘） */
        fun reload() {
            runCatching { xsp?.reload() }
        }

        override fun getBoolean(key: String, default: Boolean): Boolean {
            reload()
            return xsp?.getBoolean(key, default) ?: default
        }

        override fun getString(key: String, default: String): String {
            reload()
            return xsp?.getString(key, default) ?: default
        }

        override fun getInt(key: String, default: Int): Int {
            reload()
            return xsp?.getInt(key, default) ?: default
        }

        override fun getLong(key: String, default: Long): Long {
            reload()
            return xsp?.getLong(key, default) ?: default
        }

        override fun getFloat(key: String, default: Float): Float {
            reload()
            return xsp?.getFloat(key, default) ?: default
        }

        override fun getStringSet(key: String, default: Set<String>): Set<String> {
            reload()
            return xsp?.getStringSet(key, default) ?: default
        }

        override fun contains(key: String): Boolean {
            reload()
            return xsp?.contains(key) ?: false
        }

        override fun all(): Map<String, *> {
            reload()
            return xsp?.all ?: emptyMap<String, Any>()
        }

        /** XSharedPreferences 底层文件路径（调试用） */
        val filePath: String?
            get() = xsp?.file?.absolutePath
    }
}
