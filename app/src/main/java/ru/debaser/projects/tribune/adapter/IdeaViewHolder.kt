package ru.debaser.projects.tribune.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.idea_item_view.view.*
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.utils.getUserId
import java.text.SimpleDateFormat
import java.util.*

class IdeaViewHolder(adapter: IdeaAdapter, view: View) : RecyclerView.ViewHolder(view) {

    init {
        with (itemView) {
            avatarIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.list[currentPosition]
                    adapter.onAvatarClickListener?.onAvatarClickListener(item)
                }
            }
            likeIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.list[currentPosition]
                    adapter.onLikeClickListener?.onLikeClickListener(item, currentPosition)
                }
            }
            dislikeIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.list[currentPosition]
                    adapter.onDislikeClickListener?.onDislikeClickListener(item, currentPosition)
                }
            }
            votesIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.list[currentPosition]
                    adapter.onVotesClickListener?.onVotesClickListener(item)
                }
            }
        }
    }

    fun bind(idea: IdeaModel) {
        with (itemView) {
            if (idea.avatar.isNotEmpty()) loadImages(avatarIv, idea.avatar)
            authorTv.text = idea.author
            dateTv.text = SimpleDateFormat("dd MMMM").format(Date(idea.created))
            when {
                idea.isHater -> badgeTv.text = context.getString(R.string.hater)
                idea.isPromoter -> badgeTv.text = context.getString(R.string.promoter)
            }
            if (idea.link.isEmpty()) linkIv.visibility = View.INVISIBLE
            contentTv.text = idea.content
            loadImages(imageIv, idea.media)

            val likesCount = idea.likes.size
            when {
                likesCount <= 0 -> likesTv.visibility = View.INVISIBLE
                likesCount in 1..999 -> {
                    if (likesTv.visibility == View.INVISIBLE) likesTv.visibility = View.VISIBLE
                    likesTv.text = likesCount.toString()
                }
            }
            when {
                idea.likeActionPerforming -> {
                    likesTv.setTextColor(Color.BLUE)
                    likeIv.setColorFilter(Color.BLUE)
                }
                idea.likes.contains(context.getUserId()) -> {
                    likesTv.setTextColor(Color.GREEN)
                    likeIv.setColorFilter(Color.GREEN)
                }
            }

            val dislikesCount = idea.dislikes.size
            when {
                dislikesCount <= 0 -> dislikesTv.visibility = View.INVISIBLE
                dislikesCount in 1..999 -> {
                    if (dislikesTv.visibility == View.INVISIBLE) dislikesTv.visibility = View.VISIBLE
                    dislikesTv.text = dislikesCount.toString()
                }
            }
            when {
                idea.likeActionPerforming -> {
                    dislikesTv.setTextColor(Color.BLUE)
                    dislikeIv.setColorFilter(Color.BLUE)
                }
                idea.dislikes.contains(context.getUserId()) -> {
                    dislikesTv.setTextColor(Color.RED)
                    dislikeIv.setColorFilter(Color.RED)
                }
            }
        }
    }

    private fun loadImages(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

}