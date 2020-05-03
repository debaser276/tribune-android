package ru.debaser.projects.tribune.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.adapter.IdeaAdapter
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.notifyObserver
import java.io.IOException

class IdeasByAuthorViewModel(
    private val ideaAdapter: IdeaAdapter,
    private val authorId: Long
) : ViewModel() {

    private var currentState: State<IdeaModel>

    private val _ideas = MutableLiveData<MutableList<IdeaModel>>()
    val ideas: LiveData<MutableList<IdeaModel>>
        get() = _ideas

    private val _showLoadingDialogEvent = MutableLiveData<Boolean>()
    val showLoadingDialogEvent: LiveData<Boolean>
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
            _showLoadingDialogEvent.value = true
            getRecent()
        }
    }

    private inner class EmptyProgress:
        State<IdeaModel> {
        override fun fail() {
            currentState = EmptyError()
            _showLoadingDialogEvent.value = false
            _showEmptyErrorEvent.value = true
        }
        override fun newData(list: List<IdeaModel>) {
            _showLoadingDialogEvent.value = false
            if (list.isEmpty()) {
                _showToastEvent.value = R.string.no_idea_user
            } else {
                currentState = Data()
                _ideas.value = list.toMutableList()
            }
        }
        override fun release() {
            _showLoadingDialogEvent.value = false
            _noAuthEvent.value = true
        }
    }

    private inner class EmptyError:
        State<IdeaModel> {
        override fun refresh() {
            currentState = EmptyProgress()
            _showLoadingDialogEvent.value = true
            getRecent()
        }
    }

    private inner class Data:
        State<IdeaModel> {
        override fun refresh() {
            currentState = Refresh()
            getAfter()
        }
        override fun loadMore() {
            currentState = AddProgress()
            _showProgressBarEvent.value = true
            getBefore()
        }
    }

    private inner class Refresh:
        State<IdeaModel> {
        override fun newData(list: List<IdeaModel>) {
            currentState = Data()
            _cancelRefreshingEvent.value = true
            with (_ideas) {
                value?.addAll(0, list)
                notifyObserver()
            }
        }
        override fun fail() {
            currentState = Data()
            _showLoadingDialogEvent.value = false
            _showToastEvent.value = R.string.error_occured
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
                with (_ideas) {
                    value?.addAll(list)
                    notifyObserver()
                }
            }
        }
        override fun fail() {
            currentState = Data()
            _showProgressBarEvent.value = false
            _showToastEvent.value = R.string.error_occured
        }
    }

    private inner class AllData:
        State<IdeaModel> {
        override fun refresh() {
            currentState = Refresh()
            getAfter()
        }
    }

    private fun getRecent() {
        viewModelScope.launch {
            try {
                val result = Repository.getRecentByAuthor(authorId)
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
                val response = Repository.getAfterByAuthor(authorId, ideaAdapter.list[0].id)
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
                val response = Repository.getBeforeByAuthor(authorId, ideaAdapter.list[ideaAdapter.list.size - 1].id)
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
            idea.likeActionPerforming = true
            ideaAdapter.notifyItemChanged(position, IdeaAdapter.PAYLOAD_LIKE)
            try {
                val response = Repository.like(idea.id)
                if (response.isSuccessful) {
                    idea.updateLikes(response.body()!!)
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occured
            } finally {
                idea.likeActionPerforming = false
                ideaAdapter.notifyItemChanged(position, IdeaAdapter.PAYLOAD_LIKE)
            }
        }
    }

    fun dislikeClick(idea: IdeaModel, position: Int) {
        viewModelScope.launch {
            idea.dislikeActionPerforming = true
            try {
                ideaAdapter.notifyItemChanged(position, IdeaAdapter.PAYLOAD_DISLIKE)
                val response = Repository.dislike(idea.id)
                if (response.isSuccessful) {
                    idea.updateDislikes(response.body()!!)
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occured
            } finally {
                idea.dislikeActionPerforming = false
                ideaAdapter.notifyItemChanged(position, IdeaAdapter.PAYLOAD_DISLIKE)
            }
        }
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