package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.trust.TrustAllHostnameVerifier
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.xprefs.HookPrefs
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import hk.kirk.trustme.trust.TrustAllManager

/**
 * 第三方库 Hook — xutils / httpclientandroidlib
 */
object ThirdPartyHook {

    fun hook(classLoader: ClassLoader) {
        hookXutils(classLoader)
        hookHttpClientAndroidLib(classLoader)
    }

    /**
     * xutils 3.x HTTP 库
     */
    private fun hookXutils(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("org.xutils.http.RequestParams")

            // setSslSocketFactory → 替换为空 SSLFactory
            findAndHookMethod(
                "org.xutils.http.RequestParams", classLoader,
                "setSslSocketFactory", SSLSocketFactory::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("thirdparty")) return
                        param.args[0] = createEmptySSLFactory()
                    }
                }
            )

            // setHostnameVerifier → 替换为 TrustAllHostnameVerifier
            findAndHookMethod(
                "org.xutils.http.RequestParams", classLoader,
                "setHostnameVerifier", HostnameVerifier::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("thirdparty")) return
                        param.args[0] = TrustAllHostnameVerifier.getInstance()
                    }
                }
            )

            Logger.d("xutils RequestParams → 已 Hook")
        } catch (_: ClassNotFoundException) {
            Logger.d("xutils 未发现，跳过")
        } catch (e: Throwable) {
            Logger.e("xutils Hook 失败", e)
        }
    }

    /**
     * httpclientandroidlib (ch.boye)
     */
    private fun hookHttpClientAndroidLib(classLoader: ClassLoader) {
        try {
            classLoader.loadClass("ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier")
            findAndHookMethod(
                "ch.boye.httpclientandroidlib.conn.ssl.AbstractVerifier", classLoader,
                "verify",
                String::class.java, Array<String>::class.java,
                Array<String>::class.java, Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("thirdparty")) return
                        param.result = null
                    }
                }
            )
            Logger.d("httpclientandroidlib AbstractVerifier → 已绕过")
        } catch (_: ClassNotFoundException) {
            Logger.d("httpclientandroidlib 未发现，跳过")
        } catch (e: Throwable) {
            Logger.e("httpclientandroidlib Hook 失败", e)
        }
    }

    private fun createEmptySSLFactory(): SSLSocketFactory? {
        return try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf<TrustManager>(TrustAllManager.getInstance()), null)
            sslContext.socketFactory
        } catch (_: Exception) {
            null
        }
    }
}
