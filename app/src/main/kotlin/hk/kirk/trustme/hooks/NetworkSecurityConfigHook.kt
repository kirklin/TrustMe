package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.xprefs.HookPrefs
import java.security.cert.X509Certificate

/**
 * Android 7+ Network Security Config Hook
 */
object NetworkSecurityConfigHook {

    fun hook(classLoader: ClassLoader) {
        // checkPins(List) → DO_NOTHING
        try {
            findAndHookMethod(
                "android.security.net.config.NetworkSecurityTrustManager",
                classLoader,
                "checkPins",
                List::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("nsc")) return
                        param.result = null
                    }
                }
            )
            Logger.d("NetworkSecurityTrustManager.checkPins → 已绕过")
        } catch (_: Throwable) {
        }

        // checkServerTrusted(X509Certificate[], String) → DO_NOTHING
        try {
            findAndHookMethod(
                "android.security.net.config.NetworkSecurityTrustManager",
                classLoader,
                "checkServerTrusted",
                Array<X509Certificate>::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("nsc")) return
                        param.result = null
                    }
                }
            )
            Logger.d("NetworkSecurityTrustManager.checkServerTrusted → 已绕过")
        } catch (_: Throwable) {
        }
    }
}
