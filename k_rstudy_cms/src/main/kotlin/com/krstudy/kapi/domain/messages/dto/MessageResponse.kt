package com.krstudy.kapi.domain.messages.dto

import com.krstudy.kapi.domain.messages.entity.Message
import org.bouncycastle.asn1.cms.RecipientInfo
import java.time.LocalDateTime

data class MessageResponse(
    val id: Long,
    val content: String,
    val title: String,
    val senderId: Long?,
    val senderName: String? = null,
    val senderUserId: String? = null,
    val recipients: List<RecipientDto>,
    val sentAt: LocalDateTime,
    val readAt: LocalDateTime?
) {
    companion object {
        fun fromMessage(message: Message, currentUserId: Long? = null): MessageResponse {
            val sender = message.sender
            return MessageResponse(
                id = message.id,
                content = message.content,
                title = message.title,
                senderId = message.senderId,
                senderName = sender?.username,
                senderUserId = sender?.userid,
                recipients = message.recipients.map {
                    RecipientDto(
                        recipientId = it.recipientId,
                        recipientName = it.recipientName,
                        recipientUserId = it.recipientUserId
                    )
                },
                sentAt = message.sentAt,
                readAt = message.recipients.find {
                    currentUserId?.let { userId -> it.recipientId == userId } ?: false
                }?.readAt
            )
        }
    }
}