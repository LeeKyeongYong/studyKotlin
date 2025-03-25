package com.krstudy.kapi.domain.post.datas

import jakarta.validation.constraints.NotBlank

data class ModifyForm(
    @field:NotBlank val title: String = "",
    @field:NotBlank val body: String = "",
    val isPublished: Boolean = false
)
