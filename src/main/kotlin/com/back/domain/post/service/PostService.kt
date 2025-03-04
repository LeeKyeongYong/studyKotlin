package com.back.domain.post.service

import com.back.domain.post.entity.Post
import com.back.domain.post.repository.PostRepository
import org.springframework.stereotype.Service

@Service
class PostService(
    private val postRepository: PostRepository
) {
    fun findAll() = postRepository.findAll()

    fun write(title: String, content: String): Post {
        val post = Post(
            title = title,
            content = content
        )
        return postRepository.save(post)
    }

    fun count() = postRepository.count()
}