package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import hk.kirk.trustme.utils.PrefsHelper
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.utils.Logger
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSession

/**
 * OkHttp 2.x / 3.x / 4.x+ CertificatePinner & HostnameVerifier Hook
 *
 * 通过 Application.attach() 的 ClassLoader 调用，确保 Multi-dex 支持
 */
object OkHttpHook {

    fun hook(classLoader: ClassLoader) {
        hookOkHttp2(classLoader)
        hookOkHttp3(classLoader)
        hookOkHttp4(classLoader)
        hookOkHostnameVerifier(classLoader)
        hookFindMatchingPins(classLoader)
    }

    /** OkHttp 2.x — com.squareup.okhttp.CertificatePinner */
    private fun hookOkHttp2(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("com.squareup.okhttp.CertificatePinner")
            findAndHookMethod(
                "com.squareup.okhttp.CertificatePinner", classLoader,
                "check", String::class.java, List::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.result = true
                    }
                }
            )
            Logger.d("OkHttp 2.x CertificatePinner.check → 已绕过")
        } catch (_: ClassNotFoundException) {
            Logger.d("OkHttp 2.x 未发现，跳过")
        } catch (e: Throwable) {
            Logger.e("OkHttp 2.x Hook 失败", e)
        }
    }

    /** OkHttp 3.x — okhttp3.CertificatePinner */
    private fun hookOkHttp3(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("okhttp3.CertificatePinner")
            findAndHookMethod(
                "okhttp3.CertificatePinner", classLoader,
                "check", String::class.java, List::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.result = null
                    }
                }
            )
            Logger.d("OkHttp 3.x CertificatePinner.check → 已绕过")
        } catch (_: ClassNotFoundException) {
            Logger.d("OkHttp 3.x 未发现，跳过")
        } catch (e: Throwable) {
            Logger.e("OkHttp 3.x Hook 失败", e)
        }
    }

    /** OkHttp 4.2+ (Kotlin) — check$okhttp 方法 */
    private fun hookOkHttp4(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("okhttp3.CertificatePinner")
            findAndHookMethod(
                "okhttp3.CertificatePinner", classLoader,
                "check\$okhttp",
                String::class.java,
                "kotlin.jvm.functions.Function0",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.result = null
                    }
                }
            )
            Logger.d("OkHttp 4.2+ check\$okhttp → 已绕过")
        } catch (_: ClassNotFoundException) {
        } catch (_: NoSuchMethodError) {
            Logger.d("OkHttp 4.2+ check\$okhttp 方法不存在，跳过")
        } catch (e: Throwable) {
            Logger.e("OkHttp 4.2+ Hook 失败", e)
        }
    }

    /** OkHostnameVerifier.verify — 两个重载 */
    private fun hookOkHostnameVerifier(classLoader: ClassLoader) {
        // verify(String, SSLSession)
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier")
            findAndHookMethod(
                "okhttp3.internal.tls.OkHostnameVerifier", classLoader,
                "verify", String::class.java, SSLSession::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.result = true
                    }
                }
            )
            Logger.d("OkHostnameVerifier.verify(String, SSLSession) → 已绕过")
        } catch (_: ClassNotFoundException) {
        } catch (e: Throwable) {
            Logger.e("OkHostnameVerifier(SSLSession) Hook 失败", e)
        }

        // verify(String, X509Certificate)
        try {
            classLoader.loadClass("okhttp3.internal.tls.OkHostnameVerifier")
            findAndHookMethod(
                "okhttp3.internal.tls.OkHostnameVerifier", classLoader,
                "verify", String::class.java, X509Certificate::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.result = true
                    }
                }
            )
            Logger.d("OkHostnameVerifier.verify(String, X509Certificate) → 已绕过")
        } catch (_: ClassNotFoundException) {
        } catch (e: Throwable) {
            Logger.e("OkHostnameVerifier(X509Certificate) Hook 失败", e)
        }
    }

    /** findMatchingPins — SSLUnpinning 的不同策略：清空 hostname 使查找不到 pin */
    private fun hookFindMatchingPins(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("okhttp3.CertificatePinner")
            findAndHookMethod(
                "okhttp3.CertificatePinner", classLoader,
                "findMatchingPins", String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("okhttp")) return
                        param.args[0] = ""
                    }
                }
            )
            Logger.d("OkHttp3 findMatchingPins → hostname 已清空")
        } catch (_: ClassNotFoundException) {
        } catch (_: NoSuchMethodError) {
        } catch (e: Throwable) {
            Logger.e("findMatchingPins Hook 失败", e)
        }
    }
}
