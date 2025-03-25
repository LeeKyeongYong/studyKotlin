package com.krstudy.kapi.global.interceptor

import com.krstudy.kapi.com.krstudy.kapi.global.Security.datas.JwtTokenProvider
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class WebSocketAuthInterceptor : HandshakeInterceptor {

    @Autowired
    lateinit var jwtTokenProvider: JwtTokenProvider

    private val logger: Logger = LoggerFactory.getLogger(WebSocketAuthInterceptor::class.java)

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val token = request.headers.getFirst("Authorization")?.let { header ->
            if (header.startsWith("Bearer ")) header.substring(7) else header
        }

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val userId = jwtTokenProvider.getUserIdFromToken(token)
            attributes["userId"] = userId // 예시로 사용자 ID를 attributes에 저장합니다.
        }

        return true // 핸드셰이크를 계속 진행
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // 핸드셰이크가 완료된 후에 로그를 남깁니다.
        logger.info("WebSocket Handshake completed for request: ${request.uri}")
        if (exception != null) {
            logger.error("WebSocket Handshake encountered an error: ${exception.message}")
        }
    }
}