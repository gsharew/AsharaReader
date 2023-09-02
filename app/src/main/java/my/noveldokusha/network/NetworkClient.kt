package my.noveldokusha.network

import android.content.Context
import android.net.Uri
import my.nanihadesuka.lazycolumnscrollbar.BuildConfig
import my.noveldokusha.utils.call
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

interface NetworkClient {


    suspend fun call(request: Request.Builder, followRedirects: Boolean = false): Response
    suspend fun get(url: String): Response
    suspend fun get(url: Uri.Builder): Response
}

class ScraperNetworkClient(
    cacheDir: File,
    cacheSize: Long,
    private val appContext: Context
) : NetworkClient {
    private val cookieJar = ScraperCookieJar()

    private val okhttpLoggingInterceptor = HttpLoggingInterceptor {
        Timber.v(it)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val customHostnameVerifier = HostnameVerifier { hostname, session -> true }

    val client  = OkHttpClient.Builder()
        .let {
            if (BuildConfig.DEBUG) {
                it.addInterceptor(okhttpLoggingInterceptor)
            } else it
        }
        .addInterceptor(UserAgentInterceptor())
        .addInterceptor(DecodeResponseInterceptor())
        //.addInterceptor(CloudFareVerificationInterceptor(appContext))
        .cookieJar(cookieJar)
        .cache(Cache(cacheDir, cacheSize))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
     .sslSocketFactory(UnsafeSSLSocketFactory.getSocketFactory(),   UnsafeTrustManager())
    .hostnameVerifier( customHostnameVerifier )
     .build()

    val clientWithRedirects = client
        .newBuilder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    override suspend fun call(request: Request.Builder, followRedirects: Boolean): Response {
        return if (followRedirects) clientWithRedirects.call(request) else client.call(request)
    }

    override suspend fun get(url: String) = call(getRequest(url))
    override suspend fun get(url: Uri.Builder) = call(getRequest(url.toString()))
}
