package com.example.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authManager: AuthManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = authManager.token
        
        val requestBuilder = originalRequest.newBuilder()
            .addHeader("Accept", "application/json")
            
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        val request = requestBuilder.build()
        var response = chain.proceed(request)
        
        // Dynamic fallback routing for misconfigured Laravel API route configurations:
        // Case 1: Route not found (404) or Method not allowed (405)
        if (response.code == 404 || response.code == 405) {
            val urlString = request.url.toString()
            Log.d("AuthInterceptor", "Request to $urlString failed with code ${response.code}. Attempting fallback paths.")
            
            var fallbackRequest: okhttp3.Request? = null
            
            if (urlString.contains("/api/") && !urlString.contains("/api/api/")) {
                // Try Case A: Double prefix "api/api" (Common Laravel mistake of naming route '/api/auth/login' inside api.php)
                val newUrlString = urlString.replace("/api/", "/api/api/")
                fallbackRequest = request.newBuilder().url(newUrlString).build()
                Log.d("AuthInterceptor", "Trying fallback A: $newUrlString")
            } else if (urlString.contains("/api/")) {
                // Try Case B: Direct root path (Laravel route defined in web.php or without api prefix)
                val newUrlString = urlString.replace("/api/", "/")
                fallbackRequest = request.newBuilder().url(newUrlString).build()
                Log.d("AuthInterceptor", "Trying fallback B: $newUrlString")
            }
            
            if (fallbackRequest != null) {
                response.close() // Close previous response stream
                val fallbackResponse = chain.proceed(fallbackRequest)
                if (fallbackResponse.isSuccessful) {
                    Log.d("AuthInterceptor", "Fallback request succeeded with code ${fallbackResponse.code}")
                    return fallbackResponse
                }
                
                // If the first fallback failed, try the second fallback (e.g. going from /api/ to / if we hadn't already)
                if (urlString.contains("/api/") && !urlString.contains("/api/api/")) {
                    val rootUrlString = urlString.replace("/api/", "/")
                    Log.d("AuthInterceptor", "First fallback failed. Trying fallback C: $rootUrlString")
                    val secondFallbackRequest = request.newBuilder().url(rootUrlString).build()
                    fallbackResponse.close()
                    val secondFallbackResponse = chain.proceed(secondFallbackRequest)
                    if (secondFallbackResponse.isSuccessful) {
                        Log.d("AuthInterceptor", "Fallback C succeeded with code ${secondFallbackResponse.code}")
                        return secondFallbackResponse
                    }
                    return secondFallbackResponse
                }
                
                return fallbackResponse
            }
        }
        
        return response
    }
}
