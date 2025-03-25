package com.krstudy.kapi.domain.chat.controller

import com.krstudy.kapi.domain.chat.service.ChatService
import com.krstudy.kapi.global.https.ReqData
import com.krstudy.kapi.global.https.RespData
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/chat/rooms")
class ChatRoomController(
    private val rq:ReqData,
    private val chatService: ChatService
) {
    @GetMapping
    fun getChatRooms(): String {
        return "domain/chat/chatList"
    }

    @GetMapping("/{id}")
    fun showChatRoom(@PathVariable id: Long): String {
        val chatRoom = chatService.getChatRoom(id)
        rq.setAttribute("chatRoom", chatRoom)
        return "domain/chat/chatView"
    }

    @GetMapping("/mygpt")
    fun getChatGpt(): String {
        return "domain/chat/MyGpt"
    }



}