package hk.kirk.trustme.trust

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * 信任一切的 HostnameVerifier 实现
 */
class TrustAllHostnameVerifier : HostnameVerifier {

    companion object {
        private val INSTANCE = TrustAllHostnameVerifier()

        @JvmStatic
        fun getInstance(): TrustAllHostnameVerifier = INSTANCE
    }

    override fun verify(hostname: String?, session: SSLSession?): Boolean {
        return true
    }
}
