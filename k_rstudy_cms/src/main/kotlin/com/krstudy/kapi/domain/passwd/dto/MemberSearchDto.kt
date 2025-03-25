package com.krstudy.kapi.domain.passwd.dto

import java.time.LocalDate

data class MemberSearchDto(
    val username: String? = null,
    val fromDate: LocalDate? = null,
    val toDate: LocalDate? = null,
    val roleType: String? = null,
    val useYn: String? = null
)