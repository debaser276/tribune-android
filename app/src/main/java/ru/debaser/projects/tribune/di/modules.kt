package ru.debaser.projects.tribune.di

import android.content.Context
import android.content.SharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.utils.API_SHARED_FILE
import ru.debaser.projects.tribune.viewmodel.IdeasByAuthorViewModel
import ru.debaser.projects.tribune.viewmodel.IdeasViewModel

val applicationModule = module {
    viewModel { IdeasViewModel() }
    viewModel { (authorId: Long) -> IdeasByAuthorViewModel(authorId) }
    factory<SharedPreferences>(named(API_SHARED_FILE)) {
        androidContext().getSharedPreferences(
            API_SHARED_FILE,
            Context.MODE_PRIVATE
        )
    }
}