package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel

class IdeaAdapter: RecyclerView.Adapter<IdeaViewHolder>() {

    var onAvatarClickListener: OnAvatarClickListener? = null
    var onLikeClickListener: OnLikeClickListener? = null
    var onDislikeClickListener: OnDislikeClickListener? = null
    var onVotesClickListener: OnVotesClickListener? = null
    var onLinkClickListener: OnLinkClickListener? = null

    var list = mutableListOf<IdeaModel>()

    companion object {
        const val PAYLOAD_LIKE = "payload_like"
        const val PAYLOAD_DISLIKE = "payload_dislike"
    }

    fun submit(list: MutableList<IdeaModel>) {
        val oldList = this.list
        val diffResult = DiffUtil.calculateDiff(
            IdeaDiffUtilCallback(oldList, list)
        )
        this.list = list
        diffResult.dispatchUpdatesTo(this)
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

    class IdeaDiffUtilCallback(
        private val oldList: List<IdeaModel>,
        private val newList: List<IdeaModel>
    ): DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            val set = mutableListOf<String>()
            val isLikeActionTheSame = old.likeActionPerforming == new.likeActionPerforming
            val isDislikeActionTheSame = old.dislikeActionPerforming == new.dislikeActionPerforming
            if (isLikeActionTheSame.not()) set.add(PAYLOAD_LIKE)
            if (isDislikeActionTheSame.not()) set.add(PAYLOAD_DISLIKE)
            return set
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val res = oldList[oldItemPosition].equals(newList[newItemPosition])
            return res
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