package ru.debaser.projects.tribune.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.idea_item_view.view.*
import ru.debaser.projects.tribune.model.IdeaModel
import ru.debaser.projects.tribune.model.IdeaUiModel
import ru.debaser.projects.tribune.utils.getUserId
import java.text.SimpleDateFormat
import java.util.*

class IdeaViewHolder(adapter: IdeaAdapter, view: View) : RecyclerView.ViewHolder(view) {

    init {
        with (itemView) {
            avatarIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.ideas[currentPosition]
                    adapter.onAvatarClickListener?.onAvatarClickListener(item)
                }
            }
            likeIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.ideas[currentPosition]
                    adapter.onLikeClickListener?.onLikeClickListener(item, currentPosition)
                }
            }
            dislikeIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.ideas[currentPosition]
                    adapter.onDislikeClickListener?.onDislikeClickListener(item, currentPosition)
                }
            }
            votesIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.ideas[currentPosition]
                    adapter.onVotesClickListener?.onVotesClickListener(item)
                }
            }
            linkIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.ideas[currentPosition]
                    adapter.onLinkClickListener?.onLinkClickListener(item)
                }
            }
        }
    }

    fun bind(ideaUiModel: IdeaUiModel) {
        with (itemView) {
            with (ideaUiModel) {
                if (idea.avatar.isNotEmpty()) loadImages(avatarIv, idea.avatar)
                authorTv.text = idea.author
                dateTv.text = dateFormatted
                if (idea.link.isEmpty()) linkIv.visibility = View.INVISIBLE
                contentTv.text = idea.content
                loadImages(imageIv, idea.media)

                when (idea.isHater) {
                    true -> haterTv.visibility = View.VISIBLE
                    false -> haterTv.visibility = View.GONE
                }

                when (idea.isPromoter) {
                    true -> promoterTv.visibility = View.VISIBLE
                    false -> promoterTv.visibility = View.GONE
                }

                likesTv.setTextColor(contentTv.textColors)
                likeIv.setColorFilter(Color.BLACK)

                dislikesTv.setTextColor(contentTv.textColors)
                dislikeIv.setColorFilter(Color.BLACK)

                like(idea)

                dislike(idea)
            }
        }
    }

    fun dislike(idea: IdeaModel) {
        with (itemView) {
            val dislikesCount = idea.dislikes.size
            when {
                dislikesCount <= 0 -> dislikesTv.visibility = View.INVISIBLE
                dislikesCount in 1..999 -> {
                    if (dislikesTv.visibility == View.INVISIBLE) dislikesTv.visibility =
                        View.VISIBLE
                    dislikesTv.text = dislikesCount.toString()
                }
            }
            when {
                idea.dislikeActionPerforming -> {
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

    fun like(idea: IdeaModel) {
        with (itemView) {
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
        }
    }

    private fun loadImages(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

}