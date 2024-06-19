package com.example.showpokemonlist

import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor (private val userAgent: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(newRequest)
    }
}