package com.example.management.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var sessionManager: SessionManager? = null
    private var cachedService: ClinicApiService? = null
    private var currentBaseUrl: String? = null

    fun init(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    fun getSessionManager(context: Context): SessionManager {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
        return sessionManager!!
    }

    fun getApiService(context: Context): ClinicApiService {
        val session = getSessionManager(context)
        val targetBaseUrl = session.baseUrl

        // Nếu service chưa tạo hoặc Base URL đã bị thay đổi, khởi tạo lại
        if (cachedService == null || currentBaseUrl != targetBaseUrl) {
            currentBaseUrl = targetBaseUrl

            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor { chain ->
                    val original = chain.request()
                    val requestBuilder = original.newBuilder()

                    // Gắn token nếu đã đăng nhập
                    val token = session.getAuthToken()
                    if (!token.isNullOrBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }

                    requestBuilder.addHeader("Accept", "application/json")
                    chain.proceed(requestBuilder.build())
                }
                .addInterceptor(loggingInterceptor)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(targetBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            cachedService = retrofit.create(ClinicApiService::class.java)
        }

        return cachedService!!
    }

    fun resetService() {
        cachedService = null
    }
}
