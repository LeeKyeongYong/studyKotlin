package com.krstudy.kapi.domain.messages.entity

import com.krstudy.kapi.domain.messages.dto.RecipientDto
import com.krstudy.kapi.domain.messages.entity.Message
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class MessageRecipient(
    @ManyToOne
    @JoinColumn(name = "message_id")
    val message: Message,

    val recipientId: Long,
    var recipientName: String,
    var recipientUserId: String?, // nullable로 변경
    var readAt: LocalDateTime? = null
) : BaseEntity()