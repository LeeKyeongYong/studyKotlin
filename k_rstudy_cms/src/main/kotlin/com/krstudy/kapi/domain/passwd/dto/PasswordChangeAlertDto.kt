package com.krstudy.kapi.domain.passwd.dto

import java.time.LocalDateTime

data class PasswordChangeAlertDto(
    val memberId: Long,
    val lastPasswordChangeDate: LocalDateTime,
    val nextChangeDate: LocalDateTime,
    val daysUntilChange: Long
)