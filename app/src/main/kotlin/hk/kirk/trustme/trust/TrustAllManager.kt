package hk.kirk.trustme.trust

import android.os.Build
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 信任一切的 X509TrustManager 实现
 */
class TrustAllManager : X509TrustManager {

    companion object {
        private val INSTANCE = TrustAllManager()
        private val INSTANCE_ARRAY = arrayOf<TrustManager>(INSTANCE)

        @JvmStatic
        fun getInstance(): TrustAllManager = INSTANCE

        @JvmStatic
        fun getInstanceArray(): Array<TrustManager> = INSTANCE_ARRAY

        /**
         * 根据 API level 返回最合适的 TrustManager
         * Android N+ 使用 ExtendedTrustManager，否则使用普通版本
         */
        @JvmStatic
        fun getBestInstance(): X509TrustManager {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                TrustAllExtendedManager.getInstance()
            } else {
                INSTANCE
            }
        }

        @JvmStatic
        fun getBestInstanceArray(): Array<TrustManager> {
            return arrayOf(getBestInstance())
        }
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // 信任一切 — 不做任何检查
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        // 信任一切 — 不做任何检查
    }

    /**
     * 适配 X509TrustManagerExtensions.checkServerTrusted(chain, authType, host) 的调用
     * 返回空 List 表示验证通过
     */
    fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?,
        host: String?
    ): List<X509Certificate> {
        return ArrayList()
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}
