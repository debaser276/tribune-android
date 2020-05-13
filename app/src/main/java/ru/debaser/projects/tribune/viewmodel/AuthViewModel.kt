package ru.debaser.projects.tribune.viewmodel

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named
import retrofit2.Response
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.repository.Me
import ru.debaser.projects.tribune.repository.Repository
import ru.debaser.projects.tribune.utils.*
import java.io.IOException

class AuthViewModel(
    private val repository: Repository,
    private val sharedPref: SharedPreferences
) : ViewModel() {

    private val _passwordErrorEvent = MutableLiveData<Boolean>()
    val passwordErrorEvent: LiveData<Boolean>
        get() = _passwordErrorEvent

    private val _showLoadingDialogEvent = MutableLiveData<Int?>()
    val showLoadingDialogEvent: LiveData<Int?>
        get() = _showLoadingDialogEvent

    private val _moveToIdeasFragmentEvent = MutableLiveData<Boolean>()
    val moveToIdeasFragmentEvent: LiveData<Boolean>
        get() = _moveToIdeasFragmentEvent

    private val _showToastEvent = SingleLiveEvent<Int>()
    val showToastEvent: LiveData<Int>
        get() = _showToastEvent

    fun authenticate(login: String, password: String) {
        if (!isValid(password)) {
            _passwordErrorEvent.value = true
        } else {
            viewModelScope.launch {
                _showLoadingDialogEvent.value = R.string.authentication
                try {
                    val response =
                        repository.authenticate(login, password)
                    if (response.isSuccessful) {
                        setUserAuth(response)
                        _moveToIdeasFragmentEvent.value = true
                    } else {
                        _showToastEvent.value = R.string.authentication_failed
                    }
                } catch (e: IOException) {
                    _showToastEvent.value = R.string.error_occurred
                } finally {
                    _showLoadingDialogEvent.value = null
                }
            }
        }
    }

    fun isAuthenticated() =
        sharedPref.getString(AUTHENTICATED_SHARED_TOKEN, "")?.isNotEmpty() ?: false

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