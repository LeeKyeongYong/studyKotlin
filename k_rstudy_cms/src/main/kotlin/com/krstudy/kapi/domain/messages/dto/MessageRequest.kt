package com.krstudy.kapi.domain.messages.dto

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.messages.entity.Message
import com.krstudy.kapi.domain.messages.entity.MessageRecipient

data class MessageRequest(
    val content: String,
    val recipientId: Long,
    val recipientUserId: String?, // nullable로 변경
    var title: String
) {
    fun toMessage(currentUser: Member): Message {
        val message = Message(
            content = content,
            senderId = currentUser.id,
            recipients = mutableListOf(),
            title = title
        )

        message.recipients.add(
            MessageRecipient(
                message = message,
                recipientId = recipientId,
                recipientName = currentUser.username ?: "사용자이름없어요!", // username이 null인 경우 기본값 설정
                recipientUserId = recipientUserId ?: "아이디가 누락되었어요!" // recipientUserId가 null인 경우 기본값 설정
            )
        )

        return message
    }
}
