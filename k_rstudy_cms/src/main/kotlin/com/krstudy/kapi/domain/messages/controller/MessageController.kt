package com.krstudy.kapi.domain.messages.controller

import com.krstudy.kapi.domain.messages.service.MessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import org.springframework.ui.Model

@Controller
class MessageController(
    private val messageService: MessageService
) {
    private val logger = LoggerFactory.getLogger(MessageController::class.java)

    @GetMapping("/messages")
    suspend fun showMessageList(): String {
        return "domain/messages/messagesList"
    }

 @GetMapping("/messages/writerForm")
    suspend fun showNewMessageForm(): String {//메세지 작성하기
        return "domain/messages/writerMessage"
    }

    //메세지  상세보기페이지
    @GetMapping("/messages/{id}")
    suspend fun showMessageDetail(@PathVariable id: Long, model: Model): String {
        val message = messageService.getMessageById(id)
        val sender = messageService.getMemberById(message.senderId)

        // 메시지를 읽은 것으로 표시 (현재 사용자가 수신자인 경우)
        val currentUser = messageService.getCurrentUser()
        message.recipients.find { it.recipientId == currentUser.id }?.let { recipient ->
            if (recipient.readAt == null) {
                messageService.markAsRead(message.id, currentUser.id)
            }
        }

        model.addAttribute("message", message)
        model.addAttribute("sender", sender)

        return "domain/messages/messageDetail"
    }

}
