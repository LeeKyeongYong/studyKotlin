package com.krstudy.kapi.member.datas

import org.springframework.web.multipart.MultipartFile

data class MemberUpdateData(
    val nickname: String? = null,
    val userEmail: String? = null,
    val password: String? = null,
    val useYn: String? = null,
    val accountType: String? = null,
    val roleType: String? = null,
    val jwtToken: String? = null,
    val passwordConfirm: String? = null,  // passwordConfirm 추가
    val image: MultipartFile? = null

)