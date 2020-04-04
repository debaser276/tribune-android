package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel

class IdeaAdapter(private val list: MutableList<IdeaModel>): RecyclerView.Adapter<IdeaViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder =
        IdeaViewHolder(
            this,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.idea_item_view, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bind(list[position])
    }
}