package com.krstudy.kapi.domain.passwd.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class MemberSignature(

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    val signatureData: String, // Base64로 인코딩된 Canvas 데이터

): BaseEntity(){}
