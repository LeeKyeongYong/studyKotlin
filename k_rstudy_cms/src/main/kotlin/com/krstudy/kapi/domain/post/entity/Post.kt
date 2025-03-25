package com.krstudy.kapi.domain.post.entity

import com.querydsl.core.annotations.QueryEntity
import com.krstudy.kapi.domain.comment.entity.PostComment
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.FetchType.LAZY
import java.util.ArrayList

@Entity
@Table(name = "post")
class Post(
    @OneToMany(mappedBy = "post", cascade = [ALL], orphanRemoval = true)
    var likes: MutableList<PostLike> = ArrayList(),

    @OneToMany(mappedBy = "post", cascade = [ALL], orphanRemoval = true)
    @OrderBy("id DESC")
    var comments: MutableList<PostComment> = ArrayList(),

    @ManyToOne(fetch = FetchType.EAGER)
    var author: Member? = null,

    var title: String? = null,

    @Column(columnDefinition = "TEXT")
    var body: String? = null,

    var isPublished: Boolean = false,

    var hit: Long = 0
) : BaseEntity() {

    fun increaseHit() {
        hit++
    }

    fun addLike(member: Member) {
        if (!hasLike(member)) {
            likes.add(PostLike(post = this, member = member))
        }
    }

    fun hasLike(member: Member): Boolean {
        return likes.any { it.member == member }
    }

    fun deleteLike(member: Member) {
        likes.removeIf { it.member == member }
    }

    fun writeComment(actor: Member, body: String): PostComment {
        val postComment = PostComment(author = actor, post = this, body = body)
        comments.add(postComment)
        return postComment
    }

    // username을 가져오는 메소드
    fun getAuthorUsername(): String? {
        return author?.username
    }
}