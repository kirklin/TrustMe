package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper

/**
 * Conscrypt Socket 层验证 Hook
 *
 * 这些是比 TrustManagerImpl 更底层的 Hook 点，
 * 直接在 SSL Socket 层面绕过证书链验证。
 */
object ConscryptSocketHook {

    fun hook(classLoader: ClassLoader) {
        // OpenSSLSocketImpl.verifyCertificateChain — 旧版 Android
        hookVerifyCertificateChain(
            classLoader,
            "com.android.org.conscrypt.OpenSSLSocketImpl"
        )

        // ConscryptFileDescriptorSocket.verifyCertificateChain — Android 12+
        hookVerifyCertificateChain(
            classLoader,
            "com.android.org.conscrypt.ConscryptFileDescriptorSocket"
        )

        // Platform.checkServerTrusted
        hookPlatformCheckServerTrusted(classLoader)
    }

    private fun hookVerifyCertificateChain(classLoader: ClassLoader, className: String) {
        try {
            val cls = findClass(className, classLoader)
            // 查找所有名为 verifyCertificateChain 的方法并 Hook
            for (method in cls.declaredMethods) {
                if (method.name == "verifyCertificateChain") {
                    val params = mutableListOf<Any>()
                    params.addAll(method.parameterTypes.toList())
                    params.add(object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            PrefsHelper.reload()
                            if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("conscryptsocket")) return
                            param.result = null
                        }
                    })

                    try {
                        findAndHookMethod(className, classLoader, "verifyCertificateChain", *params.toTypedArray())
                        Logger.d("$className.verifyCertificateChain → 已绕过")
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
            // 类不存在，正常情况（不同 Android 版本）
        }
    }

    private fun hookPlatformCheckServerTrusted(classLoader: ClassLoader) {
        try {
            val cls = findClass("com.android.org.conscrypt.Platform", classLoader)
            for (method in cls.declaredMethods) {
                if (method.name == "checkServerTrusted") {
                    val params = mutableListOf<Any>()
                    params.addAll(method.parameterTypes.toList())
                    params.add(object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            PrefsHelper.reload()
                            if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("conscryptsocket")) return
                            param.result = null
                        }
                    })

                    try {
                        findAndHookMethod(
                            "com.android.org.conscrypt.Platform", classLoader,
                            "checkServerTrusted", *params.toTypedArray()
                        )
                        Logger.d("Conscrypt Platform.checkServerTrusted → 已绕过")
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }
}
