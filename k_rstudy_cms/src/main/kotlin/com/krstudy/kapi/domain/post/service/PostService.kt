package com.krstudy.kapi.domain.post.service

import com.krstudy.kapi.domain.comment.repository.PostCommentRepository
import com.krstudy.kapi.domain.comment.entity.PostComment
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.domain.post.entity.PostLike
import com.krstudy.kapi.domain.post.repository.PostRepository
import com.krstudy.kapi.domain.post.repository.PostlikeRepository
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLIntegrityConstraintViolationException
import java.util.Optional

@Service
@Transactional(readOnly = true)
class PostService(
    private val postRepository: PostRepository,
    private val postCommentRepository: PostCommentRepository,
    private val postlikeRepository: PostlikeRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(PostService::class.java)
    @Transactional
    fun write(author: Member, title: String, body: String, isPublished: Boolean): Post {
        val post = Post(author = author, title = title, body = body, isPublished = isPublished)
        return postRepository.save(post)
    }

    fun findTop30ByIsPublishedOrderByIdDesc(isPublished: Boolean): List<Post> {
        return postRepository.findTop30ByIsPublishedOrderByIdDesc(isPublished)
    }

    fun findById(id: Long): Optional<Post> {
        return postRepository.findById(id)
    }

    fun search(kw: String, pageable: Pageable): Page<Post> {
        return postRepository.search(null, true, kw, pageable)
    }

    fun search(author: Member?, isPublished: Boolean?, kw: String, pageable: Pageable): Page<Post> {
        return postRepository.search(author, isPublished, kw, pageable)
    }

    @Transactional
    fun modify(post: Post, title: String, body: String, published: Boolean) {
        post.title = title
        post.body = body
        post.isPublished = published
    }

    @Transactional
    fun delete(post: Post) {
        postRepository.delete(post)
    }

    @Transactional
    fun increaseHit(post: Post) {
        post.increaseHit()
    }

    @Transactional
    fun like(member: Member, post: Post) {
        val existingLike = postlikeRepository.findByMemberAndPost(member, post)
        if (existingLike == null) {
            val postLike = PostLike(post, member)
            postlikeRepository.save(postLike)

        } else {
            throw GlobalException(MessageCode.ALREADY_LIKED)
        }
    }



    @Transactional
    fun cancelLike(actor: Member, post: Post) {
        if (canCancelLike(actor, post)) {
            post.deleteLike(actor)
        }
    }

    @Transactional
    fun writeComment(actor: Member, post: Post, body: String): PostComment {
        val postComment = PostComment(author = actor, post = post, body = body)
        return postCommentRepository.save(postComment)
    }

    fun canLike(member: Member?, post: Post): Boolean {
        //return actor != null && !post.hasLike(actor)
        return member != null && !postlikeRepository.existsByPostAndMember(post, member)
    }

    fun canCancelLike(actor: Member?, post: Post): Boolean {
        return actor != null && post.hasLike(actor)
    }

    fun canModify(actor: Member?, post: Post): Boolean {
        return actor != null && actor == post.author
    }

    fun canDelete(actor: Member?, post: Post): Boolean {
        return actor != null && (actor.isAdmin || actor == post.author)
    }

    fun canModifyComment(actor: Member?, comment: PostComment): Boolean {
        return actor != null && actor == comment.author
    }

    fun canDeleteComment(actor: Member?, comment: PostComment): Boolean {
        return actor != null && (actor.isAdmin || actor == comment.author)
    }

    fun findCommentById(id: Long): Optional<PostComment> {
        return postCommentRepository.findCommentById(id)
    }

    @Transactional
    fun modifyComment(postComment: PostComment, body: String) {
        postComment.body = body
    }

    @Transactional
    fun deleteComment(postComment: PostComment) {
        postCommentRepository.delete(postComment)
    }

    // 리포지토리 메소드 호출
    fun findAuthorNameByPostId(postId: Long, authorId: Long): String {
        return postRepository.findAuthorNameByPostId(postId, authorId)
    }
}