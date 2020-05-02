package ru.debaser.projects.tribune.di

import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.viewmodel.IdeasByAuthorViewModel
import ru.debaser.projects.tribune.viewmodel.IdeasViewModel

val applicationModule = module {
    viewModel { (ideaAdapter: IdeaAdapter) -> IdeasViewModel(ideaAdapter) }
    viewModel { (ideaAdapter: IdeaAdapter, authorId: Long) -> IdeasByAuthorViewModel(ideaAdapter, authorId) }
}