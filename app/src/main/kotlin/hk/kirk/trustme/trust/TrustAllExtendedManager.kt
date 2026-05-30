package hk.kirk.trustme.trust

import android.annotation.TargetApi
import android.os.Build
import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.X509ExtendedTrustManager

/**
 * 信任一切的 X509ExtendedTrustManager 实现 (Android N+)
 *
 * Android N 引入了 X509ExtendedTrustManager，增加了带 Socket 和 SSLEngine 参数的变体。
 * 如果只实现基础的 X509TrustManager，某些框架可能会绕过我们的空实现。
 */
@TargetApi(Build.VERSION_CODES.N)
class TrustAllExtendedManager : X509ExtendedTrustManager() {

    companion object {
        private val INSTANCE = TrustAllExtendedManager()

        @JvmStatic
        fun getInstance(): TrustAllExtendedManager = INSTANCE
    }

    // === 基础 X509TrustManager 方法 ===

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

    // === X509ExtendedTrustManager 扩展方法 (Socket 变体) ===

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?
    ) {
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        socket: Socket?
    ) {
    }

    // === X509ExtendedTrustManager 扩展方法 (SSLEngine 变体) ===

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?
    ) {
    }

    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        engine: SSLEngine?
    ) {
    }
}
