package com.krstudy.kapi.domain.comment.entity



import com.krstudy.kapi.domain.member.entity.Member // 올바른 경로로 수정
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*

@Entity
class PostComment(
    @ManyToOne
    var author: Member? = null,

    @ManyToOne
    var post: Post? = null,

    @Column(columnDefinition = "TEXT")
    var body: String? = null
) : BaseEntity() {

    // 기본 생성자 추가
    constructor() : this(null, null, null)

    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var author: Member? = null
        private var post: Post? = null
        private var body: String? = null

        fun author(author: Member) = apply { this.author = author }
        fun post(post: Post) = apply { this.post = post }
        fun body(body: String) = apply { this.body = body }
        fun build() = PostComment(author, post, body)
    }
}
