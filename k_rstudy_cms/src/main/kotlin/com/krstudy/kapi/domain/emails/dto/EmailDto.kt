package com.krstudy.kapi.domain.emails.dto

import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

data class EmailDto(
    var id: Long? = null, // 이메일 ID 추가
    val serviceEmail: String?,  // 기본 이메일 주소
    var customEmail: String = "", // 파라미터로 받은 이메일 주소
    var title: String = "", // 메일 제목
    var content: String = "", // 메일 내용
    val receiverEmail: String?, // 받는사람 이메일 주소
    var createDate: LocalDateTime? = null, // 보낸시간
    var modifyDate: LocalDateTime? = null, // 수정시간
    var attachment: MultipartFile? = null // 첨부 파일
)