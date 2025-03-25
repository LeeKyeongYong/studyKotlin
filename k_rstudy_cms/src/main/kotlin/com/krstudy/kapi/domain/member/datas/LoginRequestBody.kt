package com.krstudy.kapi.domain.member.datas

import jakarta.validation.constraints.NotBlank

data class LoginRequestBody(
    @field:NotBlank val username: String,
    @field:NotBlank val password: String
)