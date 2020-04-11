package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.VoteModel

class VoteAdapter(val list: List<VoteModel>): RecyclerView.Adapter<VoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder =
        VoteViewHolder(
            this,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.vote_item_view, parent, false)
        )


    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(list[position])
    }
}