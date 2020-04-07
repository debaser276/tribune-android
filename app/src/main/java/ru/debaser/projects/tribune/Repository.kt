package ru.debaser.projects.tribune

import android.graphics.Bitmap
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.debaser.projects.tribune.api.API
import ru.debaser.projects.tribune.api.AuthRequestParams
import ru.debaser.projects.tribune.api.Image
import ru.debaser.projects.tribune.api.PostIdeaRequest
import ru.debaser.projects.tribune.api.interceptor.InjectAuthTokenInterceptor
import java.io.ByteArrayOutputStream

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

    suspend fun postIdea(postIdeaRequest: PostIdeaRequest) = API.postIdea(postIdeaRequest)

    suspend fun uploadImage(bitmap: Bitmap): Response<Image> {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val reqFile = RequestBody.create(MediaType.parse("image/jpeg"), bos.toByteArray())
        val body = MultipartBody.Part.createFormData("file", "image.jpg", reqFile)
        return API.uploadImage(body)
    }
}