package ru.debaser.projects.tribune.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.repository.Repository
import java.io.IOException

class IdeasByAuthorViewModel(
    private val repository: Repository,
    private val authorId: Long
) : ViewModel() {

    private var currentState: State<IdeaModel>
    var ideas = mutableListOf<IdeaModel>()

    private val _changeIdeasEvent = MutableLiveData<Boolean>()
    val changeIdeasEvent: LiveData<Boolean>
        get() = _changeIdeasEvent

    private val _showLoadingDialogEvent = MutableLiveData<Int?>()
    val showLoadingDialogEvent: LiveData<Int?>
        get() = _showLoadingDialogEvent

    private val _noAuthEvent = MutableLiveData<Boolean>()
    val noAuthEvent: LiveData<Boolean>
        get() = _noAuthEvent

    private val _showEmptyErrorEvent = MutableLiveData<Boolean>()
    val showEmptyErrorEvent: LiveData<Boolean>
        get() = _showEmptyErrorEvent

    private val _cancelRefreshingEvent = MutableLiveData<Boolean>()
    val cancelRefreshingEvent: LiveData<Boolean>
        get() = _cancelRefreshingEvent

    private val _showToastEvent = SingleLiveEvent<Int>()
    val showToastEvent: LiveData<Int>
        get() = _showToastEvent

    private val _showProgressBarEvent = MutableLiveData<Boolean>()
    val showProgressBarEvent: LiveData<Boolean>
        get() = _showProgressBarEvent

    init {
        currentState = Empty()
        currentState.refresh()
    }

    private interface State<T> {
        fun refresh() {}
        fun fail() {}
        fun newData(list: List<T>) {}
        fun release() {}
        fun loadMore() {}
    }

    private inner class Empty:
        State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            _showLoadingDialogEvent.value = R.string.getting_ideas
            getRecent()
            _changeIdeasEvent.value = true
        }
    }

    private inner class EmptyProgress:
        State<IdeaModel> {
        override fun fail() {
            currentState = EmptyError()
            _showLoadingDialogEvent.value = null
            _showEmptyErrorEvent.value = true
        }
        override fun newData(list: List<IdeaModel>) {
            _showLoadingDialogEvent.value = null
            if (list.isEmpty()) {
                _showToastEvent.value = R.string.no_idea_user
            } else {
                currentState = Data()
                ideas.addAll(list)
                _changeIdeasEvent.value = true
            }
        }
        override fun release() {
            _showLoadingDialogEvent.value = null
            _noAuthEvent.value = true
        }
    }

    private inner class EmptyError:
        State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            _showLoadingDialogEvent.value = R.string.getting_ideas
            getRecent()
            _changeIdeasEvent.value = true
        }
    }

    private inner class Data:
        State<IdeaModel> {
        override fun refresh() {
            currentState = Refresh()
            getAfter()
            _changeIdeasEvent.value = true
        }
        override fun loadMore() {
            currentState = AddProgress()
            _showProgressBarEvent.value = true
            getBefore()
            _changeIdeasEvent.value = true
        }
    }

    private inner class Refresh:
        State<IdeaModel> {
        override fun newData(list: List<IdeaModel>) {
            currentState = Data()
            _cancelRefreshingEvent.value = true
            if (list.isNotEmpty()) {
                ideas.addAll(0, list)
                _changeIdeasEvent.value = true
            }
        }
        override fun fail() {
            currentState = Data()
            _showLoadingDialogEvent.value = null
            _showToastEvent.value = R.string.error_occurred
        }
    }

    private inner class AddProgress:
        State<IdeaModel> {
        override fun newData(list: List<IdeaModel>) {
            _showProgressBarEvent.value = false
            if (list.isEmpty()) {
                currentState = AllData()
                _showToastEvent.value = R.string.loaded_all_ideas
            } else {
                currentState = Data()
                ideas.addAll(list)
                _changeIdeasEvent.value = true
            }
        }
        override fun fail() {
            currentState = Data()
            _showProgressBarEvent.value = false
            _showToastEvent.value = R.string.error_occurred
        }
    }

    private inner class AllData:
        State<IdeaModel> {
        override fun refresh() {
            currentState = Refresh()
            getAfter()
            _changeIdeasEvent.value = true
        }
    }

    private fun getRecent() {
        viewModelScope.launch {
            try {
                val result = repository.getRecentByAuthor(authorId)
                when {
                    result.isSuccessful -> currentState.newData(result.body() ?: listOf())
                    result.code() == 401-> currentState.release()
                    else -> currentState.fail()
                }
            } catch(e: IOException) {
                currentState.fail()
            }
        }
    }

    private fun getAfter() {
        viewModelScope.launch {
            try {
                val response = repository.getAfterByAuthor(authorId, ideas[0].id)
                if (response.isSuccessful) {
                    val newIdeas = response.body()!!
                    currentState.newData(newIdeas)
                } else {
                    currentState.fail()
                }
            } catch (e: Exception) {
                currentState.fail()
            }
        }
    }

    private fun getBefore() {
        viewModelScope.launch {
            try {
                val response = repository.getBeforeByAuthor(authorId, ideas[ideas.size - 1].id)
                if (response.isSuccessful) {
                    val newIdeas = response.body()!!
                    currentState.newData(newIdeas)
                } else {
                    currentState.fail()
                }
            } catch (e: Exception) {
                currentState.fail()
            }
        }
    }

    fun likeClick(idea: IdeaModel, position: Int) {
        viewModelScope.launch {
            ideas[position].likeActionPerforming = true
            _changeIdeasEvent.value = true
            try {
                val response = repository.like(idea.id)
                if (response.isSuccessful) {
                    ideas[position].updateLikes(response.body()!!)
                    _changeIdeasEvent.value = true
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                ideas[position].likeActionPerforming = false
                _changeIdeasEvent.value = true
            }
        }
    }

    fun dislikeClick(idea: IdeaModel, position: Int) {
        viewModelScope.launch {
            ideas[position].dislikeActionPerforming = true
            _changeIdeasEvent.value = true
            try {
                val response = repository.dislike(idea.id)
                if (response.isSuccessful) {
                    ideas[position].updateDislikes(response.body()!!)
                    _changeIdeasEvent.value = true
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                ideas[position].dislikeActionPerforming = false
                _changeIdeasEvent.value = true
            }
        }
    }

    fun changeIdeasEventDone() {
        _changeIdeasEvent.value = false
    }

    fun noAuthEventDone() {
        _noAuthEvent.value = false
    }

    fun cancelRefreshingEventDone() {
        _cancelRefreshingEvent.value = false
    }

    fun refresh() {
        currentState.refresh()
    }

    fun loadMore() {
        currentState.loadMore()
    }
}