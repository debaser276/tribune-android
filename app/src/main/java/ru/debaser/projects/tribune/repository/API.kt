package ru.debaser.projects.tribune.repository

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.model.VoteModel

data class AuthRequestParams(val username: String, val password: String)

data class PushRequestParams(val pushToken: String)

data class Me(val id: Long,
              val token: String,
              val username: String,
              val isHater: Boolean,
              val isPromoter: Boolean,
              val isReader: Boolean,
              val avatar: String)

data class PostIdeaRequest(val content: String, val media: String, val link: String)

data class Image(val id: String)

interface API {

    @POST("api/v1/registration")
    suspend fun register(@Body authRequestParams: AuthRequestParams): Response<Me>

    @POST("api/v1/authentication")
    suspend fun authenticate(@Body authRequestParams: AuthRequestParams): Response<Me>

    @GET("api/v1/ideas/recent")
    suspend fun getRecent(): Response<List<IdeaModel>>

    @POST("api/v1/ideas")
    suspend fun postIdea(@Body postIdeaRequest: PostIdeaRequest): Response<Void>

    @Multipart
    @POST("api/v1/media")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Image>

    @GET("api/v1/ideas/recent/{id}")
    suspend fun getRecentByAuthor(@Path("id") authorId: Long): Response<List<IdeaModel>>

    @POST("api/v1/avatar")
    suspend fun addAvatar(@Body image: Image): Response<Void>

    @PUT("api/v1/ideas/{id}/like")
    suspend fun like(@Path("id") id: Long): Response<IdeaModel>

    @PUT("api/v1/ideas/{id}/dislike")
    suspend fun dislike(@Path("id") id: Long): Response<IdeaModel>

    @GET("api/v1/ideas/{id}/after")
    suspend fun getAfter(@Path("id") id: Long): Response<List<IdeaModel>>

    @GET("api/v1/ideas/{id}/before")
    suspend fun getBefore(@Path("id") id: Long): Response<List<IdeaModel>>

    @GET("api/v1/ideas/{id}/after/{authorId}")
    suspend fun getAfterByAuthor(@Path("id") id: Long, @Path("authorId") authorId: Long): Response<List<IdeaModel>>

    @GET("api/v1/votes/{id}")
    suspend fun getVotes(@Path("id") id: Long): Response<List<VoteModel>>

    @POST("api/v1/push")
    suspend fun registerPushToken(@Body pushRequestParams: PushRequestParams): Response<Void>
}