package com.krstudy.kapi.domain.member.datas

import com.krstudy.kapi.domain.member.entity.Member

data class AuthAndMakeTokensResponseBody(
    val member: Member,
    val accessToken: String,
    val refreshToken: String?  // nullable로 변경
)