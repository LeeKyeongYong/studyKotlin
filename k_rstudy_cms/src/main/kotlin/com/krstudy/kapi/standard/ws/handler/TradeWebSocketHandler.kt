package com.krstudy.kapi.com.krstudy.kapi.standard.ws.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.krstudy.kapi.domain.trade.dto.HogaDto
import com.krstudy.kapi.standard.ws.dto.WebSocketCommand
import com.krstudy.kapi.standard.ws.dto.WebSocketMessage
import com.krstudy.kapi.standard.ws.constant.CommandType
import com.krstudy.kapi.standard.ws.registry.WebSocketSessionRegistry
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.slf4j.LoggerFactory

@Component
class TradeWebSocketHandler(
    private val objectMapper: ObjectMapper,
    private val sessionRegistry: WebSocketSessionRegistry
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        logger.info("WebSocket 연결 성공: ${session.id}")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val command = objectMapper.readValue(message.payload, WebSocketCommand::class.java)

            when (command.type) {
                CommandType.SUBSCRIBE -> handleSubscribe(session, command)
                CommandType.UNSUBSCRIBE -> handleUnsubscribe(session, command)
                else -> logger.warn("Unsupported command type: ${command.type}")
            }
        } catch (e: Exception) {
            logger.error("메시지 처리 중 오류 발생", e)
            session.sendMessage(TextMessage(
                objectMapper.writeValueAsString(WebSocketMessage("error", "", e.message ?: "Unknown error"))
            ))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessionRegistry.removeSession(session)
        logger.info("WebSocket 연결 종료: ${session.id}, status: ${status.code}")
    }

    fun sendHogaUpdate(coinCode: String, hoga: HogaDto) {
        val message = objectMapper.writeValueAsString(
            WebSocketMessage("hoga", coinCode, hoga)
        )

        sessionRegistry.getSubscribers(coinCode).forEach { session ->
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage(message))
                }
            } catch (e: Exception) {
                logger.error("호가 업데이트 전송 실패: ${session.id}", e)
            }
        }
    }

    private fun handleSubscribe(session: WebSocketSession, command: WebSocketCommand) {
        sessionRegistry.addSubscriber(command.coinCode, session)
        logger.info("구독 추가: ${session.id} -> ${command.coinCode}")
    }

    private fun handleUnsubscribe(session: WebSocketSession, command: WebSocketCommand) {
        sessionRegistry.removeSubscriber(command.coinCode, session)
        logger.info("구독 제거: ${session.id} -> ${command.coinCode}")
    }
}