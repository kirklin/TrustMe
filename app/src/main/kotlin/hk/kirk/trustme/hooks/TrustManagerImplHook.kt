package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.utils.PrefsHelper
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.security.cert.X509Certificate
import javax.net.ssl.SSLParameters
import javax.net.ssl.SSLSession

/**
 * Conscrypt TrustManagerImpl Hook — 双策略融合
 *
 * 策略A：硬编码 Hook 已知方法签名
 * 策略B：反射动态发现 checkTrustedRecursive 所有重载
 * 额外：Hook verifyChain
 */
object TrustManagerImplHook {

    private const val IMPL_CLASS = "com.android.org.conscrypt.TrustManagerImpl"
    private val emptyList = ArrayList<X509Certificate>()

    /**
     * 运行时检查开关的替换回调 — 用于 Zygote 级别 Hook
     * 每次方法被调用时都会重新加载 prefs 并检查开关状态
     */
    private fun createDynamicReplacement() = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            PrefsHelper.reload()
            if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("conscrypt")) {
                // 开关关闭，不拦截，让原始方法正常执行
                return
            }
            // 开关打开，拦截并返回空列表
            param.result = ArrayList<X509Certificate>()
        }
    }

    /**
     * Zygote 级别 Hook — 使用 TrustMeAlready 的动态反射策略
     * 在 Zygote 进程中执行，对所有 App 生效
     */
    fun hookZygote() {
        if (!hasTrustManagerImpl()) {
            Logger.i("TrustManagerImpl 不存在，跳过 Zygote Hook")
            return
        }

        var hookedCount = 0

        // 策略B：动态反射发现 checkTrustedRecursive 的所有重载
        try {
            val cls = findClass(IMPL_CLASS, null)
            for (method in cls.declaredMethods) {
                if (!isCheckTrustedRecursive(method)) continue

                val params = mutableListOf<Any>()
                params.addAll(method.parameterTypes.toList())
                params.add(createDynamicReplacement())

                try {
                    findAndHookMethod(IMPL_CLASS, null, "checkTrustedRecursive", *params.toTypedArray())
                    hookedCount++
                    Logger.d("Zygote Hook: checkTrustedRecursive(${method.parameterTypes.joinToString { it.simpleName }})")
                } catch (e: Throwable) {
                    Logger.e("Zygote Hook checkTrustedRecursive 变体失败", e)
                }
            }
        } catch (e: Throwable) {
            Logger.e("Zygote 动态反射 Hook 失败", e)
        }

        Logger.i("Zygote 级别 Hook 完成: $hookedCount 个 checkTrustedRecursive 方法")
    }

    /**
     * App 级别 Hook — 使用 JustTrustMe 的硬编码策略
     * 补充 Zygote Hook 未覆盖的方法
     */
    fun hookApp(classLoader: ClassLoader) {
        if (!hasTrustManagerImpl()) return

        // checkServerTrusted(X509Certificate[], String) → return 0
        try {
            findAndHookMethod(
                IMPL_CLASS, classLoader,
                "checkServerTrusted",
                Array<X509Certificate>::class.java, String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        PrefsHelper.reload()
                        if (!PrefsHelper.isEnabled() || !PrefsHelper.isHookEnabled("conscrypt")) return
                        Logger.d("TrustManagerImpl.checkServerTrusted(cert[], String) → 已绕过")
                        param.result = null
                    }
                }
            )
        } catch (_: Throwable) {
        }

        // checkServerTrusted(X509Certificate[], String, String) → return ArrayList()
        try {
            findAndHookMethod(
                IMPL_CLASS, classLoader,
                "checkServerTrusted",
                Array<X509Certificate>::class.java, String::class.java, String::class.java,
                createDynamicReplacement()
            )
        } catch (_: Throwable) {
        }

        // checkServerTrusted(X509Certificate[], String, SSLSession) → return ArrayList()
        try {
            findAndHookMethod(
                IMPL_CLASS, classLoader,
                "checkServerTrusted",
                Array<X509Certificate>::class.java, String::class.java, SSLSession::class.java,
                createDynamicReplacement()
            )
        } catch (_: Throwable) {
        }

        // checkTrusted(X509Certificate[], String, SSLSession, SSLParameters, boolean)
        try {
            findAndHookMethod(
                IMPL_CLASS, classLoader,
                "checkTrusted",
                Array<X509Certificate>::class.java, String::class.java,
                SSLSession::class.java, SSLParameters::class.java,
                Boolean::class.javaPrimitiveType,
                createDynamicReplacement()
            )
        } catch (_: Throwable) {
        }

        // checkTrusted(X509Certificate[], byte[], byte[], String, String, boolean)
        try {
            findAndHookMethod(
                IMPL_CLASS, classLoader,
                "checkTrusted",
                Array<X509Certificate>::class.java, ByteArray::class.java, ByteArray::class.java,
                String::class.java, String::class.java,
                Boolean::class.javaPrimitiveType,
                createDynamicReplacement()
            )
        } catch (_: Throwable) {
        }

        // verifyChain
        try {
            val cls = findClass(IMPL_CLASS, classLoader)
            for (method in cls.declaredMethods) {
                if (method.name == "verifyChain" && method.returnType == List::class.java) {
                    val params = mutableListOf<Any>()
                    params.addAll(method.parameterTypes.toList())
                    params.add(createDynamicReplacement())
                    try {
                        findAndHookMethod(IMPL_CLASS, classLoader, "verifyChain", *params.toTypedArray())
                        Logger.d("TrustManagerImpl.verifyChain() → 已 Hook")
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
        }

        Logger.d("TrustManagerImplHook (App 级别) 已安装")
    }

    /**
     * 检查方法是否为 checkTrustedRecursive 的某个重载
     */
    private fun isCheckTrustedRecursive(method: Method): Boolean {
        if (method.name != "checkTrustedRecursive") return false
        if (!List::class.java.isAssignableFrom(method.returnType)) return false

        val returnType = method.genericReturnType
        if (returnType !is ParameterizedType) return false

        val args = returnType.actualTypeArguments
        return args.size == 1 && args[0] == X509Certificate::class.java
    }

    private fun hasTrustManagerImpl(): Boolean {
        return try {
            Class.forName(IMPL_CLASS)
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
