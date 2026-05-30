package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper

/**
 * Chromium Cronet 引擎 Hook
 * Cronet 底层使用 Conscrypt，因此 TrustManagerImplHook 通常已覆盖。
 * 这里额外 Hook Cronet 特有的 Public Key Pinning 配置。
 */
object CronetHook {

    fun hook(classLoader: ClassLoader) {
        // enablePublicKeyPinningBypassForLocalTrustAnchors(boolean) → 设为 true
        try {
            classLoader.loadClass("org.chromium.net.CronetEngine\$Builder")
            findAndHookMethod(
                "org.chromium.net.CronetEngine\$Builder", classLoader,
                "enablePublicKeyPinningBypassForLocalTrustAnchors",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("cronet")) return
                        param.args[0] = true
                        Logger.d("CronetEngine.Builder.enablePublicKeyPinningBypass → true")
                    }
                }
            )
        } catch (_: ClassNotFoundException) {
            Logger.d("Cronet 未发现，跳过")
        } catch (e: Throwable) {
            Logger.e("Cronet Hook 失败", e)
        }

        // 尝试 Hook impl 类
        try {
            classLoader.loadClass("org.chromium.net.impl.CronetEngineBuilderImpl")
            findAndHookMethod(
                "org.chromium.net.impl.CronetEngineBuilderImpl", classLoader,
                "enablePublicKeyPinningBypassForLocalTrustAnchors",
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("cronet")) return
                        param.args[0] = true
                    }
                }
            )
        } catch (_: Throwable) {
        }
    }
}
