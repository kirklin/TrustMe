package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.trust.TrustAllManager
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper
import java.security.cert.X509Certificate

/**
 * TrustManager / TrustManagerFactory Hook
 */
object TrustManagerHook {

    fun hook(classLoader: ClassLoader) {
        // Hook TrustManagerFactory.getTrustManagers()
        // 只替换非系统 TrustManager（系统的由专门 Hook 处理）
        try {
            findAndHookMethod(
                "javax.net.ssl.TrustManagerFactory",
                classLoader,
                "getTrustManagers",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("trustmanager")) return

                        val managers = param.result as? Array<*>
                        if (managers == null || managers.isEmpty()) return

                        val firstManager = managers[0] ?: return
                        val className = firstManager.javaClass.name

                        // 跳过系统/平台级 TrustManager —— 它们由专门的 Hook 处理：
                        // - com.android.org.conscrypt.TrustManagerImpl → TrustManagerImplHook
                        // - android.security.net.config.* → NetworkSecurityConfigHook
                        // 替换系统 TrustManager 会导致 WebView (Chromium) 等系统组件 SSL 初始化失败
                        if (className.startsWith("com.android.") ||
                            className.startsWith("android.")) {
                            Logger.d("TrustManagerFactory.getTrustManagers() → 跳过系统 TrustManager: $className")
                            return
                        }

                        param.result = TrustAllManager.getBestInstanceArray()
                        Logger.d("TrustManagerFactory.getTrustManagers() → 已替换 (原始: $className)")
                    }
                }
            )
        } catch (e: Throwable) {
            Logger.e("TrustManagerFactory Hook 失败", e)
        }

        // Hook X509TrustManagerExtensions.checkServerTrusted(chain, authType, host)
        try {
            findAndHookMethod(
                "android.net.http.X509TrustManagerExtensions",
                classLoader,
                "checkServerTrusted",
                Array<X509Certificate>::class.java,
                String::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("trustmanager")) return

                        // 返回类型必须是 List<X509Certificate>，不能直接返回 Array
                        @Suppress("UNCHECKED_CAST")
                        val chain = param.args[0] as? Array<X509Certificate>
                        param.result = chain?.toList() ?: emptyList<X509Certificate>()
                        Logger.d("X509TrustManagerExtensions.checkServerTrusted() → 已绕过")
                    }
                }
            )
        } catch (e: Throwable) {
            Logger.e("X509TrustManagerExtensions Hook 失败", e)
        }
    }
}
