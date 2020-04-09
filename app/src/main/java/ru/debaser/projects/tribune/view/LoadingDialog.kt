package ru.debaser.projects.tribune.view

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import ru.debaser.projects.tribune.R

class LoadingDialog(context: Context, resId: Int): AlertDialog(context) {
    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        val tvMessage = view.findViewById<TextView>(R.id.messageTv)
        setTitle(resId)
        tvMessage.text = context.getString(R.string.please_wait)
        setView(view)
        setCancelable(false)
    }
}