package ru.debaser.projects.tribune.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.VoteModel
import ru.debaser.projects.tribune.repository.Repository
import java.io.IOException

class VotesViewModel(private val repository: Repository): ViewModel() {

    private val _votes = MutableLiveData<List<VoteModel>>()
    val votes: LiveData<List<VoteModel>>
        get() = _votes

    private val _showLoadingDialogEvent = MutableLiveData<Boolean>()
    val showLoadingDialogEvent: LiveData<Boolean>
        get() = _showLoadingDialogEvent

    private val _showToastEvent = SingleLiveEvent<Int>()
    val showToastEvent: LiveData<Int>
        get() = _showToastEvent

    fun getVotes(ideaId :Long) {
        viewModelScope.launch {
            _showLoadingDialogEvent.value = true
            try {
                val result = repository.getVotes(ideaId)
                if (result.isSuccessful) {
                    _votes.value = result.body()!!
                } else {
                    _showToastEvent.value = R.string.cant_upload_image
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                _showLoadingDialogEvent.value = false
            }
        }
    }


}