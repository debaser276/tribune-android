package ru.debaser.projects.tribune.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.debaser.projects.tribune.adapter.IdeaAdapter

class IdeasByAuthorViewModelFactory(
    private val ideaAdapter: IdeaAdapter,
    private val authorId: Long
): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IdeasByAuthorViewModel::class.java)) {
            return IdeasByAuthorViewModel(
                ideaAdapter, authorId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}