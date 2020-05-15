package ru.debaser.projects.tribune.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.model.IdeaUiModel
import java.text.SimpleDateFormat

class IdeaAdapter: RecyclerView.Adapter<IdeaViewHolder>() {
    var onAvatarClickListener: OnAvatarClickListener? = null
    var onLikeClickListener: OnLikeClickListener? = null
    var onDislikeClickListener: OnDislikeClickListener? = null
    var onVotesClickListener: OnVotesClickListener? = null
    var onLinkClickListener: OnLinkClickListener? = null
    var ideas = mutableListOf<IdeaModel>()
    private val dateFormatter = SimpleDateFormat("dd MMM hh:mm")

    companion object {
        const val PAYLOAD_LIKE = "payload_like"
        const val PAYLOAD_DISLIKE = "payload_dislike"
    }

    fun submit(newList: MutableList<IdeaModel>) {
        val oldList = ideas
        val diffResult = DiffUtil.calculateDiff(
            IdeaDiffUtilCallback(oldList, newList)
        )
        ideas = newList.map { it.copy() }.toMutableList()
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder =
        IdeaViewHolder(
            this,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.idea_item_view, parent, false))

    override fun getItemCount(): Int = ideas.size

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        holder.bind(
            IdeaUiModel(
                ideas[position],
                dateFormatter.format(ideas[position].created * 1000
                )
            )
        )
    }

    override fun onBindViewHolder(
        holder: IdeaViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            for (payload in payloads) {
                when (payload) {
                    PAYLOAD_LIKE -> holder.like(ideas[position])
                    PAYLOAD_DISLIKE -> holder.dislike(ideas[position])
                }
            }
        } else {
            onBindViewHolder(holder, position)
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
            val isLikeAction = old.likeActionPerforming != new.likeActionPerforming ||
                old.likes.size != new.likes.size
            val isDislikeAction = old.dislikeActionPerforming != new.dislikeActionPerforming ||
                old.dislikes.size != new.dislikes.size
            return when {
                isLikeAction -> PAYLOAD_LIKE
                isDislikeAction -> PAYLOAD_DISLIKE
                else -> null
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].equals(newList[newItemPosition])
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