package com.krstudy.kapi.domain.passwd.dto

import org.springframework.web.multipart.MultipartFile
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.AssertTrue

data class PasswordChangeDto(
    @field:NotBlank(message = "현재 비밀번호는 필수입니다")
    val currentPassword: String,

    @field:NotBlank(message = "새 비밀번호는 필수입니다")
    val newPassword: String,

    @field:NotBlank(message = "변경 사유는 필수입니다")
    val changeReason: String,

    val signatureData: String? = null // Canvas 데이터를 Base64로 인코딩한 문자열
)