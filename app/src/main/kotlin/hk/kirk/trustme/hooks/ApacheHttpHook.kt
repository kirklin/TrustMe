package hk.kirk.trustme.hooks

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.*
import hk.kirk.trustme.trust.TrustAllManager
import hk.kirk.trustme.utils.Logger
import hk.kirk.trustme.xprefs.HookPrefs
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.SingleClientConnManager
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.HttpParams
import java.net.Socket
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Apache HTTP Client Hook
 */
object ApacheHttpHook {

    fun hook(classLoader: ClassLoader) {
        if (!hasApacheHttpClient()) {
            Logger.d("Apache HTTP Client 未发现，跳过")
            return
        }

        hookDefaultHttpClient(classLoader)
        hookSSLSocketFactory(classLoader)
        hookSchemeRegistry(classLoader)
    }

    private fun hookDefaultHttpClient(classLoader: ClassLoader) {
        // DefaultHttpClient() 无参构造
        try {
            findAndHookConstructor(
                DefaultHttpClient::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        setObjectField(param.thisObject, "defaultParams", null)
                        setObjectField(param.thisObject, "connManager", createSCCM())
                    }
                }
            )
        } catch (_: Throwable) {
        }

        // DefaultHttpClient(HttpParams)
        try {
            findAndHookConstructor(
                DefaultHttpClient::class.java, HttpParams::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        setObjectField(param.thisObject, "defaultParams", param.args[0] as HttpParams)
                        setObjectField(param.thisObject, "connManager", createSCCM())
                    }
                }
            )
        } catch (_: Throwable) {
        }

        // DefaultHttpClient(ClientConnectionManager, HttpParams)
        try {
            findAndHookConstructor(
                DefaultHttpClient::class.java, ClientConnectionManager::class.java, HttpParams::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        val params = param.args[1] as HttpParams
                        setObjectField(param.thisObject, "defaultParams", params)
                        setObjectField(
                            param.thisObject, "connManager",
                            determineConnManager(param.args[0], params)
                        )
                    }
                }
            )
        } catch (_: Throwable) {
        }

        Logger.d("DefaultHttpClient 构造函数 → 已 Hook")
    }

    private fun hookSSLSocketFactory(classLoader: ClassLoader) {
        // SSLSocketFactory 构造函数
        try {
            findAndHookConstructor(
                SSLSocketFactory::class.java,
                String::class.java, KeyStore::class.java, String::class.java,
                KeyStore::class.java, SecureRandom::class.java,
                org.apache.http.conn.scheme.HostNameResolver::class.java,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        val algorithm = param.args[0] as String
                        val keystore = param.args[1] as? KeyStore
                        val keystorePassword = param.args[2] as? String
                        val random = param.args[4] as? SecureRandom

                        var keyManagers: Array<KeyManager>? = null
                        if (keystore != null) {
                            keyManagers = callStaticMethod(
                                SSLSocketFactory::class.java,
                                "createKeyManagers", keystore, keystorePassword
                            ) as Array<KeyManager>
                        }

                        val trustManagers = TrustAllManager.getBestInstanceArray()
                        val sslContext = SSLContext.getInstance(algorithm)
                        sslContext.init(keyManagers, trustManagers, random)
                        setObjectField(param.thisObject, "sslcontext", sslContext)
                        setObjectField(
                            param.thisObject, "socketfactory",
                            sslContext.socketFactory
                        )
                    }
                }
            )
        } catch (_: Throwable) {
        }

        // SSLSocketFactory.getSocketFactory()
        try {
            findAndHookMethod(
                "org.apache.http.conn.ssl.SSLSocketFactory", classLoader,
                "getSocketFactory",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        param.result = newInstance(SSLSocketFactory::class.java)
                    }
                }
            )
        } catch (_: Throwable) {
        }

        // SSLSocketFactory.isSecure(Socket) → DO_NOTHING
        try {
            findAndHookMethod(
                "org.apache.http.conn.ssl.SSLSocketFactory", classLoader,
                "isSecure", Socket::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        param.result = null
                    }
                }
            )
        } catch (_: Throwable) {
        }

        Logger.d("Apache SSLSocketFactory → 已 Hook")
    }

    private fun hookSchemeRegistry(classLoader: ClassLoader) {
        // 替换 HTTPS scheme
        try {
            findAndHookMethod(
                "org.apache.http.conn.scheme.SchemeRegistry", classLoader,
                "register", Scheme::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (!HookPrefs.isHookActive("apache")) return
                        val scheme = param.args[0] as Scheme
                        if (scheme.name == "https") {
                            param.args[0] = Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
                        }
                    }
                }
            )
        } catch (_: Throwable) {
        }
    }

    // === 辅助方法 ===

    private fun createSCCM(): ClientConnectionManager? {
        return try {
            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
            trustStore.load(null, null)
            val sf = TrustAllApacheSSLSocketFactory(trustStore)
            sf.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            val registry = SchemeRegistry()
            registry.register(Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
            registry.register(Scheme("https", sf, 443))
            SingleClientConnManager(null, registry)
        } catch (_: Exception) {
            null
        }
    }

    private fun createTSCCM(params: HttpParams): ClientConnectionManager? {
        return try {
            val trustStore = KeyStore.getInstance(KeyStore.getDefaultType())
            trustStore.load(null, null)
            val sf = TrustAllApacheSSLSocketFactory(trustStore)
            sf.hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
            val registry = SchemeRegistry()
            registry.register(Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
            registry.register(Scheme("https", sf, 443))
            ThreadSafeClientConnManager(params, registry)
        } catch (_: Exception) {
            null
        }
    }

    private fun determineConnManager(original: Any, params: HttpParams): ClientConnectionManager? {
        return when (original.javaClass.simpleName) {
            "SingleClientConnManager" -> createSCCM()
            "ThreadSafeClientConnManager" -> createTSCCM(params)
            else -> null
        }
    }

    private fun hasApacheHttpClient(): Boolean {
        return try {
            Class.forName("org.apache.http.impl.client.DefaultHttpClient")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    /** 信任一切的 Apache SSLSocketFactory */
    class TrustAllApacheSSLSocketFactory(truststore: KeyStore) : SSLSocketFactory(truststore) {
        private val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(TrustAllManager.getInstance()), null)
        }

        override fun createSocket(socket: Socket, host: String, port: Int, autoClose: Boolean): Socket {
            return sslContext.socketFactory.createSocket(socket, host, port, autoClose)
        }

        override fun createSocket(): Socket {
            return sslContext.socketFactory.createSocket()
        }
    }
}
