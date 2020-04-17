package ru.debaser.projects.tribune.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.vote_item_view.view.*
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.VoteModel
import java.text.SimpleDateFormat
import java.util.*

class VoteViewHolder(adapter: VoteAdapter, view: View): RecyclerView.ViewHolder(view) {

    init {
        with (itemView) {
            avatarIv.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val item = adapter.list[currentPosition]
                    adapter.onAvatarClickListener?.onAvatarClickListener(item)
                }
            }
        }
    }

    fun bind(vote: VoteModel) {
        with (itemView) {
            with (thumbIv) {
                if (vote.isUp) {
                    setImageResource(R.drawable.ic_thumb_up)
                    setColorFilter(Color.GREEN)
                } else {
                    setImageResource(R.drawable.ic_thumb_down)
                    setColorFilter(Color.RED)
                }
            }
            if (vote.avatar.isNotEmpty()) loadImages(avatarIv, vote.avatar)
            authorTv.text = vote.author
            createdTv.text = SimpleDateFormat("dd MMMM").format(Date(vote.created))
        }
    }

    private fun loadImages(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

}