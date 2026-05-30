package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper

/**
 * Conscrypt PinManager Hook
 */
object PinManagerHook {

    private const val PIN_MANAGER_CLASS = "com.android.org.conscrypt.PinManager"

    fun hook(classLoader: ClassLoader) {
        try {
            val cls = findClass(PIN_MANAGER_CLASS, classLoader)

            // isChainValid(String, List) → return true
            try {
                findAndHookMethod(
                    PIN_MANAGER_CLASS, classLoader,
                    "isChainValid", String::class.java, List::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            PrefsHelper.reload()
                            if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("pinmanager")) return
                            param.result = true
                        }
                    }
                )
                Logger.d("PinManager.isChainValid → 已绕过")
            } catch (_: NoSuchMethodError) {
            }

            // 动态查找并 Hook 所有 checkPins 方法
            for (method in cls.declaredMethods) {
                if (method.name == "checkPins") {
                    val params = mutableListOf<Any>()
                    params.addAll(method.parameterTypes.toList())
                    params.add(object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            PrefsHelper.reload()
                            if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("pinmanager")) return
                            param.result = null
                        }
                    })
                    try {
                        findAndHookMethod(PIN_MANAGER_CLASS, classLoader, "checkPins", *params.toTypedArray())
                        Logger.d("PinManager.checkPins → 已绕过")
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
            // PinManager 不存在，正常情况
        }
    }
}
