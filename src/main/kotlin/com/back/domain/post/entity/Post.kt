package com.back.domain.post.entity

import com.back.global.base.entity.BaseTime
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class Post(
    @Column(length = 100)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String
) : BaseTime()