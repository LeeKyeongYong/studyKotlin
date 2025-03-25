package com.krstudy.kapi.domain.chat.controller

import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import com.krstudy.kapi.global.https.RespData
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/gpt")
class ChatRestController(
    private val chatClient: ChatClient
) {
    private val logger = LoggerFactory.getLogger(ChatRestController::class.java)

    @GetMapping("/chat")
    fun chat(@RequestParam(required = false) message: String?): ResponseEntity<RespData<String>> {
        // 메시지 검증
        if (message.isNullOrBlank()) {
            logger.warn("Empty or null message received.")
            throw GlobalException(
                resultCode = "400-2",
                msg = "메시지를 입력해 주세요."
            )
        }

        // 받은 메시지 로그 출력
        logger.info("Received message: {}", message)

        // 챗봇 응답 생성
        val response = try {
            chatClient.prompt()
                .user(message)
                .call()
                .content()
        } catch (e: Exception) {
            logger.error("Chat generation error", e)
            throw GlobalException(
                resultCode = "500-1",
                msg = "채팅 생성 중 오류가 발생했습니다."
            )
        }

        // 응답 로그 출력 후 반환
        logger.info("Chat response: {}", response)
        return ResponseEntity.ok(
            RespData.of(
                MessageCode.SUCCESS.code,
                MessageCode.SUCCESS.message,
                response
            )
        )
    }
}