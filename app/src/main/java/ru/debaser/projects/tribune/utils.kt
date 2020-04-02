package ru.debaser.projects.tribune

import java.util.regex.Pattern

private val pattern  by lazy (LazyThreadSafetyMode.NONE) {
    Pattern.compile("(?=.*[A-Z])[a-zA-Z0-9]{6,}")
}

fun isValid(password: String) = pattern.matcher(password).matches()