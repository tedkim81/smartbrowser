package com.teuskim.sbrowser

import java.io.IOException

import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.params.HttpClientParams
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.conn.ConnectTimeoutException
import org.apache.http.conn.scheme.PlainSocketFactory
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.scheme.SchemeRegistry
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.params.HttpProtocolParams

object HttpManager {

    private var sClient: DefaultHttpClient? = null

    fun init() {

        val params = BasicHttpParams()

        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1)
        HttpProtocolParams.setContentCharset(params, "UTF-8")

        HttpConnectionParams.setStaleCheckingEnabled(params, false)
        HttpConnectionParams.setConnectionTimeout(params, 30 * 1000)
        HttpConnectionParams.setSoTimeout(params, 30 * 1000)
        HttpConnectionParams.setSocketBufferSize(params, 8192)

        HttpClientParams.setRedirecting(params, true)

        val schemeRegistry = SchemeRegistry()
        schemeRegistry.register(Scheme("http", PlainSocketFactory.getSocketFactory(), 80))
        schemeRegistry.register(Scheme("https", SSLSocketFactory.getSocketFactory(), 443))

        val manager = ThreadSafeClientConnManager(params, schemeRegistry)
        sClient = DefaultHttpClient(manager, params)

    }

    @Throws(IOException::class, ClientProtocolException::class, ConnectTimeoutException::class)
    fun execute(request: HttpUriRequest): HttpResponse {
        return sClient!!.execute(request)
    }

    @Throws(IOException::class, ClientProtocolException::class, ConnectTimeoutException::class)
    fun execute(request: HttpUriRequest, responseHandler: ResponseHandler<*>): Any {
        return sClient!!.execute(request, responseHandler)
    }

}
