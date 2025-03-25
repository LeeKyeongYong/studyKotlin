package com.krstudy.kapi.domain.passwd.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class PasswordChangeHistory(


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(nullable = false)
    val changeReason: String,

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    val signatureData: String? = null,  // Canvas 서명 데이터를 저장할 필드 추가

    @Column(nullable = false)
    val changedAt: LocalDateTime = LocalDateTime.now()
):BaseEntity(){}