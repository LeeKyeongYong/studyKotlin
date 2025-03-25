package com.krstudy.kapi.domain.comment.repository

import com.krstudy.kapi.domain.comment.entity.PostComment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PostCommentRepository : JpaRepository<PostComment, Long> {
    fun findCommentById(id: Long): Optional<PostComment>
    @Modifying
    @Query("DELETE FROM PostComment c WHERE c.author.id = :authorId")
    fun deleteByAuthorId(@Param("authorId") authorId: Long)
}