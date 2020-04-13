package ru.debaser.projects.tribune.model

import ru.debaser.projects.tribune.utils.BASE_URL



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

    val mediaUrl
        get() = "${BASE_URL}api/v1/static/$media"
    val avatarUrl
        get() = "${BASE_URL}api/v1/static/$avatar"

    fun updateLikes(idea: IdeaModel) {
        likes = idea.likes
    }

    fun updateDislikes(idea: IdeaModel) {
        dislikes = idea.dislikes
    }
}