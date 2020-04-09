package ru.debaser.projects.tribune.repository

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.debaser.projects.tribune.model.IdeaModel

data class AuthRequestParams(val username: String, val password: String)

data class Me(val id: Long, val token: String, val isHater: Boolean, val isPromoter: Boolean, val isReader: Boolean)

data class PostIdeaRequest(val content: String, val media: String, val link: String)

data class Image(val id: String)

interface API {

    @POST("api/v1/registration")
    suspend fun register(@Body authRequestParams: AuthRequestParams): Response<Me>

    @POST("api/v1/authentication")
    suspend fun authenticate(@Body authRequestParams: AuthRequestParams): Response<Me>

    @GET("api/v1/ideas/recent")
    suspend fun getRecentIdeas(): Response<List<IdeaModel>>

    @POST("api/v1/ideas")
    suspend fun postIdea(@Body postIdeaRequest: PostIdeaRequest): Response<Void>

    @Multipart
    @POST("api/v1/media")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Image>

    @GET("api/v1/ideas/recent/{id}")
    suspend fun getRecentByAuthor(@Path("id") authorId: Long): Response<List<IdeaModel>>

}