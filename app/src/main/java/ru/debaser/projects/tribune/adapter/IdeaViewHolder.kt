package ru.debaser.projects.tribune.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.idea_item_view.view.*
import ru.debaser.projects.tribune.R
import ru.debaser.projects.tribune.model.IdeaModel

class IdeaViewHolder(adapter: IdeaAdapter, view: View) : RecyclerView.ViewHolder(view) {

    fun bind(idea: IdeaModel) {
        with (itemView) {
            authorTv.text = idea.author
            dateTv.text = idea.created.toString()
            when {
                idea.isHater -> badgeTv.text = context.getString(R.string.hater)
                idea.isPromoter -> badgeTv.text = context.getString(R.string.promoter)
            }
            contentTv.text = idea.content
            loadImages(imageIv, idea.url)
        }
    }

    private fun loadImages(imageIv: ImageView, url: String) {
        Glide.with(imageIv).load(url).into(imageIv)
    }

}