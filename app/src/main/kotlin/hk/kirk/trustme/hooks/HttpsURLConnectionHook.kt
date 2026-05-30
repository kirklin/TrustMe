package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory

/**
 * HttpsURLConnection Hook
 */
object HttpsURLConnectionHook {

    fun hook(classLoader: ClassLoader) {
        val callback = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                PrefsHelper.reload()
                if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("urlconnection")) return
                param.result = null
            }
        }

        // setDefaultHostnameVerifier → DO_NOTHING
        try {
            findAndHookMethod(
                "javax.net.ssl.HttpsURLConnection", classLoader,
                "setDefaultHostnameVerifier",
                HostnameVerifier::class.java,
                callback
            )
            Logger.d("HttpsURLConnection.setDefaultHostnameVerifier → 已无效化")
        } catch (e: Throwable) {
            Logger.e("setDefaultHostnameVerifier Hook 失败", e)
        }

        // setSSLSocketFactory → DO_NOTHING
        try {
            findAndHookMethod(
                "javax.net.ssl.HttpsURLConnection", classLoader,
                "setSSLSocketFactory",
                SSLSocketFactory::class.java,
                callback
            )
            Logger.d("HttpsURLConnection.setSSLSocketFactory → 已无效化")
        } catch (e: Throwable) {
            Logger.e("setSSLSocketFactory Hook 失败", e)
        }

        // setHostnameVerifier → DO_NOTHING
        try {
            findAndHookMethod(
                "javax.net.ssl.HttpsURLConnection", classLoader,
                "setHostnameVerifier",
                HostnameVerifier::class.java,
                callback
            )
            Logger.d("HttpsURLConnection.setHostnameVerifier → 已无效化")
        } catch (e: Throwable) {
            Logger.e("setHostnameVerifier Hook 失败", e)
        }
    }
}
