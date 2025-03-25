package com.krstudy.kapi.domain.post.repository


import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.post.entity.Post
import org.hibernate.query.Page

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param


interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom {
    fun findTop30ByIsPublishedOrderByIdDesc(isPublished: Boolean): List<Post>

    @Query("SELECT get_author_name(:authorId) FROM Post p WHERE p.id = :postId")
    fun findAuthorNameByPostId(@Param("postId") postId: Long, @Param("authorId") authorId: Long): String

    @Modifying
    @Query("DELETE FROM Post p WHERE p.author.id = :authorId")
    fun deleteByAuthorId(@Param("authorId") authorId: Long)
}