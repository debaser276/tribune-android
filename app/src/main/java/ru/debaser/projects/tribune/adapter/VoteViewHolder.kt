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
        itemView.setOnClickListener {
            val currentPosition = adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val item = adapter.votes[currentPosition]
                adapter.onItemClickListener?.onItemClickListener(item)
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
            when (vote.isHater) {
                true -> haterTv.visibility = View.VISIBLE
                false -> haterTv.visibility = View.GONE
            }

            when (vote.isPromoter) {
                true -> promoterTv.visibility = View.VISIBLE
                false -> promoterTv.visibility = View.GONE
            }
            authorTv.text = vote.author
            createdTv.text = SimpleDateFormat("dd MMMM hh:mm").format(Date(vote.created * 1000))
        }
    }

    private fun loadImages(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

}