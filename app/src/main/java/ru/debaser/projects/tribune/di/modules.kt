package ru.debaser.projects.tribune.di

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.repository.AuthTokenSource
import ru.debaser.projects.tribune.repository.PreferencesAuthTokenSource
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.repository.interceptor.InjectAuthTokenInterceptor
import ru.debaser.projects.tribune.utils.API_SHARED_FILE
import ru.debaser.projects.tribune.utils.BASE_URL
import ru.debaser.projects.tribune.view.LoadingDialog
import ru.debaser.projects.tribune.viewmodel.*

val applicationModule = module {
    viewModel { IdeasViewModel(get()) }
    viewModel { (authorId: Long) -> IdeasByAuthorViewModel(get(), authorId) }
    viewModel { VotesViewModel(get()) }
    viewModel { AuthViewModel(get(), get(named(API_SHARED_FILE))) }
    viewModel { RegViewModel(get(), get(named(API_SHARED_FILE))) }
    viewModel { PostIdeaViewModel(get()) }
    factory { (context: Context) -> LoadingDialog(context) }
    factory<SharedPreferences>(named(API_SHARED_FILE)) {
        androidContext().getSharedPreferences(
            API_SHARED_FILE,
            Context.MODE_PRIVATE
        )
    }
    factory<AuthTokenSource> { PreferencesAuthTokenSource(get(named(API_SHARED_FILE))) }
    single<Interceptor> { InjectAuthTokenInterceptor(get()) }
    single {
        val client = OkHttpClient.Builder()
            .addInterceptor(get())
            .addInterceptor(HttpLoggingInterceptor())
            .build()
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>().create(ru.debaser.projects.tribune.repository.API::class.java)
    }
    single { Repository(get()) }
}