package ru.debaser.projects.tribune.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.idea_item_view.view.*
import kotlinx.android.synthetic.main.idea_item_view.view.authorTv
import kotlinx.android.synthetic.main.idea_item_view.view.avatarIv
import kotlinx.android.synthetic.main.vote_item_view.view.*
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.VoteModel
import java.text.SimpleDateFormat
import java.util.*

class VoteViewHolder(adapter: VoteAdapter, view: View): RecyclerView.ViewHolder(view) {

    fun bind(vote: VoteModel) {
        with (itemView) {
            with (votesIv) {
                if (vote.isUp) {
                    setImageResource(R.drawable.ic_thumb_up)
                    setColorFilter(R.color.green)
                } else {
                    setImageResource(R.drawable.ic_thumb_down)
                    setColorFilter(R.color.red)
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