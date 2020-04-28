package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel

class IdeaAdapter(var list: MutableList<IdeaModel>): RecyclerView.Adapter<IdeaViewHolder>() {

    var onAvatarClickListener: OnAvatarClickListener? = null
    var onLikeClickListener: OnLikeClickListener? = null
    var onDislikeClickListener: OnDislikeClickListener? = null
    var onVotesClickListener: OnVotesClickListener? = null
    var onLinkClickListener: OnLinkClickListener? = null

    companion object {
        const val PAYLOAD_LIKE = "payload_like"
        const val PAYLOAD_DISLIKE = "payload_dislike"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder =
        IdeaViewHolder(
            this,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.idea_item_view, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun onBindViewHolder(
        holder: IdeaViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            for (payload in payloads) {
                when (payload) {
                    PAYLOAD_LIKE -> holder.like(list[position])
                    PAYLOAD_DISLIKE -> holder.dislike(list[position])
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    interface OnAvatarClickListener {
        fun onAvatarClickListener(ideaModel: IdeaModel)
    }

    interface OnLikeClickListener {
        fun onLikeClickListener(idea: IdeaModel, position: Int)
    }

    interface OnDislikeClickListener {
        fun onDislikeClickListener(idea: IdeaModel, position: Int)
    }

    interface OnVotesClickListener {
        fun onVotesClickListener(idea: IdeaModel)
    }

    interface OnLinkClickListener {
        fun onLinkClickListener(idea: IdeaModel)
    }
}