/**
 * For unknown reasons KU is using self signed certificates, thereby removing most of the
 * advantages of using SSL in the first place. Hence the need for these workarounds.
 */
package dk.ku.sund.smartsleep.manager

import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import javax.security.cert.CertificateException

open class TrustAllClient : WebViewClient() {
    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        handler?.proceed()
    }
}

fun trustKU() {
    try {
        val defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier()
        HttpsURLConnection.setDefaultHostnameVerifier(object : HostnameVerifier {
            override fun verify(hostname: String, session: SSLSession): Boolean {
                if (hostname.endsWith(".ku.dk")) return true // KU has misconfigured SSL certificates. Trust them anyway.
                return defaultVerifier.verify(hostname, session)
            }
        })
        val context = SSLContext.getInstance("TLS")
        context.init(null, arrayOf<X509TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf<X509Certificate>()
            }

            @Throws(CertificateException::class)
            override fun checkClientTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(
                chain: Array<X509Certificate>,
                authType: String
            ) {
            }
        }), SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(
            context.getSocketFactory()
        )
    } catch (e: Exception) { // should never happen
        e.printStackTrace()
    }
}
