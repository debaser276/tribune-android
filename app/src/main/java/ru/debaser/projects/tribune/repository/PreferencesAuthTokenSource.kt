package ru.debaser.projects.tribune.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import ru.debaser.projects.tribune.utils.AUTHENTICATED_SHARED_TOKEN

interface AuthTokenSource {
    var authToken: String?
}

class PreferencesAuthTokenSource(private val preferences: SharedPreferences): AuthTokenSource {
    override var authToken: String?
        get() = preferences.getString(AUTHENTICATED_SHARED_TOKEN, null)
        set(value) {
            preferences.edit {
                putString(AUTHENTICATED_SHARED_TOKEN, value)
            }
        }
}