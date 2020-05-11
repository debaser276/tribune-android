package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.VoteModel

class VoteAdapter(): RecyclerView.Adapter<VoteViewHolder>() {
    var votes = mutableListOf<VoteModel>()
    var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder =
        VoteViewHolder(
            this,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.vote_item_view, parent, false)
        )

    fun submit(newList: MutableList<VoteModel>) {
        votes = newList
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = votes.size

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {
        holder.bind(votes[position])
    }

    interface OnItemClickListener {
        fun onItemClickListener(vote: VoteModel)
    }
}