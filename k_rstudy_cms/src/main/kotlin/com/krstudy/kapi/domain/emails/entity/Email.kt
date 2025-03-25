package com.krstudy.kapi.domain.emails.entity

import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Entity
class Email (
    @Value("\${spring.mail.username}")
    @Column(length = 50)
    var serviceEmail: String = "", // 기본 이메일 주소

    @Column(length = 30)
    val title: String, // 메일 제목

    @Column(length = 255)
    val content: String, // 메일 내용

    @Column(length = 50)
    var receiverEmail: String = "", // 받는사람 이메일 주소


) : BaseEntity() {}
