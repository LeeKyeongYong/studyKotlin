package com.krstudy.kapi.domain.post.repository

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.post.entity.Post
import com.krstudy.kapi.domain.post.entity.PostLike
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface PostlikeRepository : JpaRepository<PostLike, Long> {
    fun existsByPostAndMember(post: Post, member: Member): Boolean
    fun findByPostAndMember(post: Post, member: Member): PostLike?
    fun findByMemberAndPost(member: Member, post: Post): PostLike?
    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.member.id = :memberId")
    fun deleteByMemberId(@Param("memberId") memberId: Long)
}
