package com.krstudy.kapi.domain.messages.entity

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Message(
    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val content: String,

    @Column(name = "sender_id")
    val senderId: Long,  // 주석 해제하고 다시 추가

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    val sender: Member? = null,

    val sentAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "message", cascade = [CascadeType.ALL], orphanRemoval = true)
    val recipients: MutableList<MessageRecipient> = mutableListOf()
) : BaseEntity()