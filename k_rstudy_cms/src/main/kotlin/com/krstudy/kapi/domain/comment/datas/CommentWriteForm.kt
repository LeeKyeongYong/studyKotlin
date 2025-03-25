package com.krstudy.kapi.com.krstudy.kapi.domain.comment.datas

import jakarta.validation.constraints.NotBlank

data class CommentWriteForm(
    @field:NotBlank val body: String = ""
)