package hk.kirk.trustme.xprefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * Jetpack Compose 集成扩展
 *
 * 为 [XposedPrefs.ModuleSide] 提供 Compose State 驱动的配置读写，
 * 开关切换自动持久化，UI 自动 recompose。
 *
 * 用法：
 * ```kotlin
 * val prefs = XposedPrefs.module(context, "my_prefs")
 *
 * @Composable
 * fun SettingsScreen() {
 *     val enabled = prefs.rememberBooleanState("enabled", true)
 *     SwitchRow(
 *         checked = enabled.value,
 *         onCheckedChange = { enabled.value = it },
 *     )
 * }
 * ```
 */

/**
 * 可观察的 Boolean Prefs State
 *
 * 读取时返回当前值，写入时自动持久化到 SharedPreferences
 */
class PrefsBooleanState internal constructor(
    private val prefs: XposedPrefs.ModuleSide,
    private val key: String,
    initialValue: Boolean,
) : MutableState<Boolean> {

    private val delegate = mutableStateOf(initialValue)

    override var value: Boolean
        get() = delegate.value
        set(newValue) {
            delegate.value = newValue
            prefs.putBoolean(key, newValue)
        }

    override fun component1(): Boolean = value
    override fun component2(): (Boolean) -> Unit = { value = it }
}

/**
 * 记住一个与 SharedPreferences 绑定的 Boolean 状态
 *
 * 值变更时自动写入磁盘，UI 自动 recompose
 */
@Composable
fun XposedPrefs.ModuleSide.rememberBooleanState(
    key: String,
    default: Boolean = false,
): PrefsBooleanState {
    return remember(key) {
        PrefsBooleanState(this, key, getBoolean(key, default))
    }
}
