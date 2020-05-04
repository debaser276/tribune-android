package ru.debaser.projects.tribune.model

data class IdeaModel (
    val id: Long,
    val authorId: Long,
    val author: String,
    val isHater: Boolean,
    val isPromoter: Boolean,
    val avatar: String,
    val created: Long,
    val content: String,
    val media: String,
    val link: String,
    var likes: Set<Long>,
    var dislikes: Set<Long>
) {
    var likeActionPerforming = false
    var dislikeActionPerforming = false

    fun updateLikes(idea: IdeaModel) {
        likes = idea.likes
    }

    fun updateDislikes(idea: IdeaModel) {
        dislikes = idea.dislikes
    }

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) return false
        other as IdeaModel
        if (isHater != other.isHater) return false
        if (isPromoter != other.isPromoter) return false
        if (likes != other.likes) return false
        if (dislikes != other.dislikes) return false
        if (likeActionPerforming != other.likeActionPerforming) return false
        if (dislikeActionPerforming != other.dislikeActionPerforming) return false
        return true
    }
}