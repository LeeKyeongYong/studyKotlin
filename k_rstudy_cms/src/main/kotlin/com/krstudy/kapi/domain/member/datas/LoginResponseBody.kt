package com.krstudy.kapi.domain.member.datas

import com.krstudy.kapi.domain.member.dto.MemberDto
import org.springframework.lang.NonNull

data class LoginResponseBody(
    @NonNull val item: MemberDto
)