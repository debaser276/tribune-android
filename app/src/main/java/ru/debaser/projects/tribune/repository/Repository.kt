package ru.debaser.projects.tribune.repository

import android.graphics.Bitmap
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.debaser.projects.tribune.utils.BASE_URL
import ru.debaser.projects.tribune.repository.interceptor.InjectAuthTokenInterceptor
import java.io.ByteArrayOutputStream

class Repository(private val api: API) {

    suspend fun authenticate(login: String, password: String) =
        api.authenticate(AuthRequestParams(login, password))

    suspend fun register(username: String, password: String) =
        api.register(AuthRequestParams(username, password))

    suspend fun getRecent() = api.getRecent()

    suspend fun getRecentByAuthor(authorId: Long) = api.getRecentByAuthor(authorId)

    suspend fun postIdea(postIdeaRequest: PostIdeaRequest) = api.postIdea(postIdeaRequest)

    suspend fun uploadImage(bitmap: Bitmap): Response<Image> {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray())
        val body = MultipartBody.Part.createFormData("file", "image.jpg", reqFile)
        return api.uploadImage(body)
    }

    suspend fun addAvatar(id: String) = api.addAvatar(Image(id = id))

    suspend fun like(id: Long) = api.like(id)

    suspend fun dislike(id: Long) = api.dislike(id)

    suspend fun getAfter(id: Long) = api.getAfter(id)

    suspend fun getBefore(id: Long) = api.getBefore(id)

    suspend fun getAfterByAuthor(id: Long, authorId: Long) = api.getAfterByAuthor(id, authorId)

    suspend fun getBeforeByAuthor(id: Long, authorId: Long) = api.getBeforeByAuthor(id, authorId)

    suspend fun getVotes(id: Long) = api.getVotes(id)

    suspend fun registerPushToken(token: String) =
        api.registerPushToken(PushRequestParams(token))
}