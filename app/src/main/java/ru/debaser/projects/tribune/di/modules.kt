package ru.debaser.projects.tribune.di

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.viewmodel.IdeasViewModel

val applicationModule = module {
    single { (ideaAdapter: IdeaAdapter) -> IdeasViewModel(ideaAdapter) }
}