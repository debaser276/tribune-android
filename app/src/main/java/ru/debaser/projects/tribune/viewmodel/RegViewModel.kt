package ru.debaser.projects.tribune.viewmodel

import android.content.SharedPreferences
import android.graphics.Bitmap
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Response
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.repository.Me
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.*
import java.io.IOException

class RegViewModel(
    private val repository: Repository,
    private val sharedPref: SharedPreferences
): ViewModel() {

    private val _passwordErrorEvent = MutableLiveData<Int>()
    val passwordErrorEvent: LiveData<Int>
        get() = _passwordErrorEvent

    private val _showLoadingDialogEvent = MutableLiveData<Int?>()
    val showLoadingDialogEvent: LiveData<Int?>
        get() = _showLoadingDialogEvent

    private val _addingAvatarEvent = MutableLiveData<Boolean>()
    val addingAvatarEvent: LiveData<Boolean>
        get() = _addingAvatarEvent

    private val _showToastEvent = SingleLiveEvent<Int>()
    val showToastEvent: LiveData<Int>
        get() = _showToastEvent

    private val _moveToIdeasFragmentEvent = MutableLiveData<Boolean>()
    val moveToIdeasFragmentEvent: LiveData<Boolean>
        get() = _moveToIdeasFragmentEvent

    private val _avatarId = MutableLiveData<String>()
    val avatarId: LiveData<String>
        get() = _avatarId

    fun register(login: String, password: String, passwordRep: String) {
        if (password != passwordRep) {
            _passwordErrorEvent.value = R.string.passwords_not_match
        } else if (!isValid(password)) {
            _passwordErrorEvent.value = R.string.password_incorrect
        } else {
            viewModelScope.launch {
                _showLoadingDialogEvent.value = R.string.registration
                try {
                    val response = repository.register(login, password)
                    if (response.isSuccessful) {
                        setUserAuth(response)
                        _addingAvatarEvent.value = true
                    } else {
                        _showToastEvent.value = R.string.registration_failed
                    }
                } catch (e: IOException) {
                    _showToastEvent.value = R.string.error_occurred
                } finally {
                    _showLoadingDialogEvent.value = null
                }
            }
        }
    }

    fun addAvatar() {
        viewModelScope.launch {
            _showLoadingDialogEvent.value = R.string.set_avatar
            try {
                if (_avatarId.value != null) {
                    repository.addAvatar(_avatarId.value!!)
                }
                _moveToIdeasFragmentEvent.value = true
            } catch (e: IOException) {
                _showLoadingDialogEvent.value = R.string.error_occurred
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
                    _avatarId.value = imageUploadResult.body()!!.id
                } else {
                    _showToastEvent.value = R.string.cant_upload_image
                }
            } catch (e: IOException) {
                _showToastEvent.value = R.string.error_occurred
            } finally {
                _showLoadingDialogEvent.value = null
            }
        }
    }

    private fun setUserAuth(response: Response<Me>) {
        sharedPref.edit {
            putLong(AUTHENTICATED_SHARED_ID, response.body()!!.id)
            putString(AUTHENTICATED_SHARED_USERNAME, response.body()!!.username)
            putString(AUTHENTICATED_SHARED_TOKEN, response.body()!!.token)
            putBoolean(AUTHENTICATED_SHARED_ISHATER, response.body()!!.isHater)
            putBoolean(AUTHENTICATED_SHARED_ISPROMOTER, response.body()!!.isPromoter)
            putBoolean(AUTHENTICATED_SHARED_ISREADER, response.body()!!.isReader)
        }
    }

}