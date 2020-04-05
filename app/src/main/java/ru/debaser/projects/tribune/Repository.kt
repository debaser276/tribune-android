package ru.debaser.projects.tribune

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.debaser.projects.tribune.api.API
import ru.debaser.projects.tribune.api.AuthRequestParams
import ru.debaser.projects.tribune.api.interceptor.InjectAuthTokenInterceptor

object Repository {

    private var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private var API: API = retrofit.create(ru.debaser.projects.tribune.api.API::class.java)

    fun createRetrofitWithAuthToken(authToken: String) {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(InjectAuthTokenInterceptor(authToken))
            .addInterceptor(httpLoggingInterceptor)
            .build()
        retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        API = retrofit.create(ru.debaser.projects.tribune.api.API::class.java)
    }

    suspend fun authenticate(login: String, password: String) =
        API.authenticate(AuthRequestParams(login, password))

    suspend fun getRecentIdeas() = API.getRecentIdeas()
}