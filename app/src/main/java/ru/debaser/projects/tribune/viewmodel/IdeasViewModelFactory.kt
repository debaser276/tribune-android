package ru.debaser.projects.tribune.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.debaser.projects.tribune.adapter.IdeaAdapter


class IdeasViewModelFactory(private val ideaAdapter: IdeaAdapter): ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IdeasViewModel::class.java)) {
            return IdeasViewModel(
                ideaAdapter
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}