package ru.debaser.projects.tribune.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import ru.debaser.projects.tribune.repository.Repository

class RegViewModel(
    private val repository: Repository,
    private val sharedPref: SharedPreferences
): ViewModel() {

}