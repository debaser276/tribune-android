package ru.debaser.projects.tribune.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.repository.PostIdeaRequest
import ru.debaser.projects.tribune.repository.Repository
import java.io.IOException

class PostIdeaViewModel(
    private val repository: Repository
): ViewModel() {

    private val _showLoadingDialogEvent = MutableLiveData<Int?>()
    val showLoadingDialogEvent: LiveData<Int?>
        get() = _showLoadingDialogEvent

    private val _showToastEvent = SingleLiveEvent<Int>()
    val showToastEvent: LiveData<Int>
        get() = _showToastEvent

    private val _moveToIdeasFragmentEvent = MutableLiveData<Boolean>()
    val moveToIdeasFragmentEvent: LiveData<Boolean>
        get() = _moveToIdeasFragmentEvent

    private val _media = MutableLiveData<String>()
    val media: LiveData<String>
        get() = _media

    fun postIdea(postIdeaRequest: PostIdeaRequest) {
        viewModelScope.launch {
            _showLoadingDialogEvent.value = R.string.create_new_idea
            try {
                val result =
                    repository.postIdea(postIdeaRequest)
                if (result.isSuccessful) {
                    _showToastEvent.value = R.string.success_idea
                    _moveToIdeasFragmentEvent.value = true
                } else {
                    _showToastEvent.value = R.string.error_occurred
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                _showLoadingDialogEvent.value = null
            }
        }
    }

    fun uploadImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _showLoadingDialogEvent.value = R.string.image_uploading
            try {
                val imageUploadResult =
                    repository.uploadImage(bitmap)
                if (imageUploadResult.isSuccessful) {
                    _media.value = imageUploadResult.body()!!.id
                } else {
                    _showToastEvent.value = R.string.image_uploading
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                _showLoadingDialogEvent.value = null
            }
        }
    }

}