package ru.debaser.projects.tribune.adapter

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

inline fun RecyclerView.onScrolledToFooter(crossinline action: () -> Unit) {
    addOnScrollListener(
        object : RecyclerView.OnScrollListener() {

            private val linearLayoutManager = layoutManager as LinearLayoutManager

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                with(linearLayoutManager) {
                    if (dy > 0) {
                        val visibleItemCount = childCount
                        val firstVisibleItemPosition = findFirstVisibleItemPosition()
                        val totalItemCount = itemCount

                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
                            action()
                        }
                    }
                }
            }
        })
}