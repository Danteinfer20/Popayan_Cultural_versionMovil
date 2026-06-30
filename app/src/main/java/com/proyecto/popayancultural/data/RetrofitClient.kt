package com.proyecto.popayancultural.data

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitClient {
    private const val BASE_URL = "https://vivelarte.com/"

    private lateinit var tokenManager: TokenManager

    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    private val unsafeTrustManager = object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }

    private val client: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val token   = tokenManager.getToken()
            val builder = request.newBuilder()
                .header("Accept", "application/json")
            // ⚠️ FIX CLOUDINARY: NO hardcodear Content-Type aquí.
            // Para multipart/form-data (@Multipart), OkHttp lo pone
            // automáticamente con el boundary correcto.
            // Para JSON (@Body), Retrofit/Gson lo pone como application/json.
            // Si lo forzamos aquí, rompemos el multipart.
            if (token != null) {
                builder.header("Authorization", "Bearer $token")
            }
            chain.proceed(builder.build())
        }

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(unsafeTrustManager), SecureRandom())
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .sslSocketFactory(sslContext.socketFactory, unsafeTrustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val gson = GsonBuilder().setLenient().create()

    private val gsonConverterFactory = GsonConverterFactory.create(gson)

    private val safeGsonFactory = object : Converter.Factory() {
        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<out Annotation>,
            methodAnnotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<*, RequestBody>? {
            return gsonConverterFactory.requestBodyConverter(
                type, parameterAnnotations, methodAnnotations, retrofit
            )
        }

        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): Converter<ResponseBody, *> {
            val delegate = gsonConverterFactory
                .responseBodyConverter(type, annotations, retrofit)!!
            return Converter<ResponseBody, Any?> { body ->
                try { delegate.convert(body) } catch (e: Exception) { null }
            }
        }
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(safeGsonFactory)
            .build()
            .create(ApiService::class.java)
    }
}