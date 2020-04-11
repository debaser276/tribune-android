package ru.debaser.projects.tribune.model

data class VoteModel(
    val id: Long,
    val authorId: Long,
    val author: String,
    val isHater: Boolean,
    val isPromoter: Boolean,
    val avatar: String,
    val ideaId: Long,
    val created: Long,
    val isUp: Boolean
)