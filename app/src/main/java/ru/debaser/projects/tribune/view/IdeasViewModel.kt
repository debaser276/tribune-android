package ru.debaser.projects.tribune.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.debaser.projects.tribune.model.IdeaModel

class IdeasViewModel : ViewModel() {

    private val _ideas = MutableLiveData<List<IdeaModel>>()
    val ideas: LiveData<List<IdeaModel>>
        get() = _ideas

    fun getRecent() {

    }
}