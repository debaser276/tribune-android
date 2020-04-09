package ru.debaser.projects.tribune.model

import ru.debaser.projects.tribune.utils.BASE_URL

data class IdeaModel (
    val id: Long,
    val authorId: Long,
    val author: String,
    val isHater: Boolean,
    val isPromoter: Boolean,
    val created: Long,
    val content: String,
    val media: String,
    val link: String,
    val likes: Set<Long>,
    val dislikes: Set<Long>
) {
    val mediaUrl
        get() = "${BASE_URL}api/v1/static/$media"
}