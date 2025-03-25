package com.krstudy.kapi.domain.post.repository


import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.post.entity.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable


interface PostRepositoryCustom {
    fun search(author: Member?, isPublished: Boolean?, kw: String, pageable: Pageable): Page<Post>
}
