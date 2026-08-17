package hk.kirk.trustme

import android.content.Context
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import hk.kirk.trustme.hooks.*
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.LogFileWriter
import hk.kirk.trustme.xprefs.XposedPrefs

/**
 * TrustMe main entry point
 *
 * 同时实现 IXposedHookLoadPackage 和 IXposedHookZygoteInit：
 * - initZygote: 在 Zygote 进程中初始化，执行全局 Hook（Conscrypt TrustManagerImpl 动态反射）
 * - handleLoadPackage: 每个 App 加载时执行，按模块分发 Hook
 */
class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    companion object {
        private const val TAG = "TrustMe"
        private const val MY_PACKAGE = "hk.kirk.trustme"
        private const val PREFS_NAME = "trustme_prefs"
    }

    /** Hook 端的配置桥 — 通过 XposedPrefs 读取模块 UI 端的设置 */
    private val prefs = XposedPrefs.hook(MY_PACKAGE, PREFS_NAME)

    // ==========================================
    // Zygote 级别初始化（对所有进程生效）
    // ==========================================

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        Logger.i("$TAG initZygote — 模块正在加载...")

        Logger.i("$TAG Prefs 可读: ${prefs.isAvailable}, 全局开关: ${prefs.getBoolean("enabled", true)}")

        // Zygote 级别：动态反射 Hook TrustManagerImpl.checkTrustedRecursive
        // 这是最底层的 Hook 点，无需 ClassLoader
        // 注意：Hook 回调内部会动态检查开关状态，关闭开关后不会绕过 SSL
        try {
            TrustManagerImplHook.hookZygote()
            Logger.i("$TAG Zygote 级别 Conscrypt Hook 完成（回调内含运行时开关检查）")
        } catch (e: Throwable) {
            Logger.e("$TAG Zygote 级别 Hook 失败", e)
        }
    }

    // ==========================================
    // 每个 App 加载时执行
    // ==========================================

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // 对自身包名：Hook onCreate，在 UI 构建前将 isModuleActive 设为 true
        if (lpparam.packageName == MY_PACKAGE) {
            try {
                findAndHookMethod(
                    "hk.kirk.trustme.ui.SettingsActivity",
                    lpparam.classLoader,
                    "onCreate",
                    android.os.Bundle::class.java,
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val clazz = param.thisObject.javaClass
                            XposedHelpers.setStaticBooleanField(clazz, "isModuleActive", true)
                        }
                    }
                )
                Logger.i("$TAG 模块激活状态 Hook 完成")
            } catch (e: Throwable) {
                Logger.e("$TAG 模块激活状态 Hook 失败", e)
            }
            return
        }

        // 重新加载配置
        prefs.reload()

        // 检查全局开关
        if (!prefs.getBoolean("enabled", true)) {
            Logger.i("全局开关已关闭，跳过 ${lpparam.packageName} (prefs可读: ${prefs.isAvailable})")
            return
        }

        // 更新日志开关
        Logger.setLoggingEnabled(prefs.getBoolean("logging", true))

        Logger.d("处理 App: ${lpparam.packageName}")
        LogFileWriter.init(lpparam.packageName)

        val cl = lpparam.classLoader

        // ===== 系统级 Hook（直接在当前 ClassLoader 中 Hook） =====

        // 1. SSLContext.init — 底层替换 TrustManager
        hookSafely("SSLContext") { SSLContextHook.hook(cl) }

        // 2. TrustManager / TrustManagerFactory
        hookSafely("TrustManager") { TrustManagerHook.hook(cl) }

        // 3. Conscrypt TrustManagerImpl（App 级别的硬编码 Hook）
        hookSafely("Conscrypt") { TrustManagerImplHook.hookApp(cl) }

        // 4. HttpsURLConnection
        hookSafely("URLConnection") { HttpsURLConnectionHook.hook(cl) }

        // 5. WebView
        hookSafely("WebView") { WebViewHook.hook(cl) }

        // 6. Network Security Config (Android 7+)
        hookSafely("NSC") { NetworkSecurityConfigHook.hook(cl) }

        // 7. Conscrypt Socket 层
        hookSafely("ConscryptSocket") { ConscryptSocketHook.hook(cl) }

        // 8. Conscrypt PinManager
        hookSafely("PinManager") { PinManagerHook.hook(cl) }

        // ===== Multi-dex 支持：通过 Application.attach 延迟加载第三方库 Hook =====
        try {
            findAndHookMethod(
                "android.app.Application",
                cl,
                "attach",
                Context::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val context = param.args[0] as Context
                        val appClassLoader = context.classLoader

                        // 9. OkHttp (2.x / 3.x / 4.x+)
                        hookSafely("OkHttp") { OkHttpHook.hook(appClassLoader, context.applicationInfo) }

                        // 10. Apache HTTP Client
                        hookSafely("Apache") { ApacheHttpHook.hook(appClassLoader) }

                        // 11. Cronet
                        hookSafely("Cronet") { CronetHook.hook(appClassLoader) }

                        // 12. 第三方库 (xutils, httpclientandroidlib)
                        hookSafely("ThirdParty") { ThirdPartyHook.hook(appClassLoader) }
                    }
                }
            )
        } catch (e: Throwable) {
            Logger.e("Hook Application.attach 失败", e)

            // 回退：直接在当前 ClassLoader 中尝试
            hookSafely("OkHttp") { OkHttpHook.hook(cl, lpparam.appInfo) }
            hookSafely("Apache") { ApacheHttpHook.hook(cl) }
            hookSafely("Cronet") { CronetHook.hook(cl) }
            hookSafely("ThirdParty") { ThirdPartyHook.hook(cl) }
        }
    }

    /**
     * 安全执行 Hook，单个 Hook 失败不影响其他模块
     */
    private fun hookSafely(hookName: String, block: () -> Unit) {
        if (!prefs.getBoolean("hook_${hookName.lowercase()}", true)) {
            Logger.d("$hookName Hook 已禁用，跳过")
            return
        }
        try {
            Logger.currentHook = hookName
            block()
        } catch (e: Throwable) {
            Logger.e("$hookName Hook 失败: ${e.message}", e)
        }
    }
}
