package ru.debaser.projects.tribune.repository.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import ru.debaser.projects.tribune.repository.AuthTokenSource

const val AUTH_TOKEN_HEADER = "Authorization"

class InjectAuthTokenInterceptor (private val tokenSource: AuthTokenSource): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.newBuilder()
            .header(AUTH_TOKEN_HEADER, "Bearer ${tokenSource.authToken}")
            .build()
        return chain.proceed(requestWithToken)
    }
}