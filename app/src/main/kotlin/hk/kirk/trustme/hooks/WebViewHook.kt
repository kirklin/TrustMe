package hk.kirk.trustme.hooks

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.xprefs.HookPrefs

/**
 * WebView SSL 错误处理 Hook
 */
object WebViewHook {

    fun hook(classLoader: ClassLoader) {
        // onReceivedSslError → 自动 proceed
        try {
            findAndHookMethod(
                "android.webkit.WebViewClient", classLoader,
                "onReceivedSslError",
                WebView::class.java, SslErrorHandler::class.java, SslError::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("webview")) return
                        (param.args[1] as SslErrorHandler).proceed()
                        Logger.d("WebViewClient.onReceivedSslError → 已自动 proceed")
                        param.result = null
                    }
                }
            )
        } catch (e: Throwable) {
            Logger.e("WebView onReceivedSslError Hook 失败", e)
        }

        // onReceivedError → DO_NOTHING
        try {
            findAndHookMethod(
                "android.webkit.WebViewClient", classLoader,
                "onReceivedError",
                WebView::class.java, Int::class.javaPrimitiveType,
                String::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("webview")) return
                        param.result = null
                    }
                }
            )
        } catch (e: Throwable) {
            Logger.e("WebView onReceivedError Hook 失败", e)
        }
    }
}
