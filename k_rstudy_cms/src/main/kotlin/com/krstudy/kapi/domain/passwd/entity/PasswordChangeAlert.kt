package com.krstudy.kapi.domain.passwd.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class PasswordChangeAlert(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(nullable = false)
    val lastPasswordChangeDate: LocalDateTime,

    @Column(nullable = false)
    val nextChangeDate: LocalDateTime,

    @Column(nullable = false)
    var isNotified: Boolean = false,

    @Column(nullable = false)
    var isDismissed: Boolean = false
):BaseEntity(){}