package ru.debaser.projects.tribune.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ru.debaser.projects.tribune.model.IdeaModel

data class AuthRequestParams(val username: String, val password: String)

data class Me(val id: Long, val token: String, val isHater: Boolean, val isPromoter: Boolean, val isReader: Boolean)

data class PostIdea(val content: String, val media: String, val link: String)

interface API {

    @POST("api/v1/authentication")
    suspend fun authenticate(@Body authRequestParams: AuthRequestParams): Response<Me>

    @GET("api/v1/ideas/recent")
    suspend fun getRecentIdeas(): Response<MutableList<IdeaModel>>

    @POST("api/v1/ideas")
    suspend fun postIdea(@Body postIdea: PostIdea): Response<Void>

}