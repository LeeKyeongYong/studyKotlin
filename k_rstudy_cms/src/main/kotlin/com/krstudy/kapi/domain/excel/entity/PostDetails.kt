package com.krstudy.kapi.domain.excel.entity

import com.querydsl.core.annotations.Immutable
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Immutable
@Table(name = "post_details")
data class PostDetails(
    @Id
    @Column(name = "post_id")
    val postId: Long? = null,
    val postTitle: String? = null,
    val postCreateDate: LocalDateTime? = null,
    val postModifyDate: LocalDateTime? = null,
    val postHit: Int? = null,
    val postAuthorId: String? = null,
    val commentId: Long? = null,
    val commentAuthorId: String? = null,
    val commentCreateDate: LocalDateTime? = null,
    val likeMemberId: String? = null,
    val likeCreateDate: LocalDateTime? = null
)
