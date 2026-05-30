package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import hk.kirk.trustme.trust.TrustAllManager
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper
import java.security.SecureRandom
import javax.net.ssl.KeyManager
import javax.net.ssl.TrustManager

/**
 * SSLContext.init() 底层替换
 *
 * 这是最核心的 Hook 之一 —— 几乎所有 HTTPS 连接最终都会通过 SSLContext.init() 设置 TrustManager。
 * 通过在 init() 调用前替换 TrustManager 参数，实现全局的证书信任。
 */
object SSLContextHook {

    fun hook(classLoader: ClassLoader) {
        try {
            findAndHookMethod(
                "javax.net.ssl.SSLContext",
                classLoader,
                "init",
                Array<KeyManager>::class.java,
                Array<TrustManager>::class.java,
                SecureRandom::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("sslcontext")) return
                        param.args[0] = null
                        param.args[1] = TrustAllManager.getBestInstanceArray()
                        param.args[2] = null
                        Logger.d("SSLContext.init() → 已替换 TrustManager")
                    }
                }
            )
            Logger.d("SSLContextHook 已安装")
        } catch (e: Throwable) {
            Logger.e("SSLContextHook 安装失败", e)
        }
    }
}
