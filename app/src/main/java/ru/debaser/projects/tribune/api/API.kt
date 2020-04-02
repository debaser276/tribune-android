package ru.debaser.projects.tribune.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AuthRequestParams(val username: String, val password: String)

data class Me(val id: Long, val token: String, val isHater: Boolean, val isPromoter: Boolean, val isReader: Boolean)

interface API {

    @POST("api/v1/authentication")
    suspend fun authenticate(@Body authRequestParams: AuthRequestParams): Response<Me>

}