package com.krstudy.kapi.domain.member.datas

import com.krstudy.kapi.standard.base.ValidImage
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.multipart.MultipartFile

@Validated
data class JoinForm( // 회원 가입 폼 데이터 클래스: 입력 데이터 검증을 위한 클래스
    @field:NotBlank(message = "사용자 ID는 필수입니다")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]{4,20}$", message = "아이디는 4~20자의 영문, 숫자, 언더스코어만 사용 가능합니다.")
    val userid: String,

    @field:NotBlank(message = "사용자 이름은 필수입니다")
    @field:Size(min = 2, max = 50, message = "사용자 이름은 2~50자 사이여야 합니다")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다"
    )
    val password: String,

    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val userEmail: String,

    @field:ValidImage(maxSize = 100 * 1024 * 1024, types = ["image/jpeg", "image/png", "image/gif"])
    val image: MultipartFile?
)
