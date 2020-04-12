package ru.debaser.projects.tribune.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.regex.Pattern

private val pattern  by lazy (LazyThreadSafetyMode.NONE) {
    Pattern.compile("(?=.*[A-Z])[a-zA-Z0-9]{6,}")
}

fun isValid(password: String) = pattern.matcher(password).matches()

fun Fragment.toast(resId: Int) =
    Toast.makeText(context, context?.getString(resId), Toast.LENGTH_SHORT).run {
        show()
    }

fun Fragment.toast(message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).run {
        show()
    }

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Context.getUserId(): Long = this.getSharedPreferences(API_SHARED_FILE, Context.MODE_PRIVATE)
    .getLong(AUTHENTICATED_SHARED_ID, 0)

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}