package com.krstudy.kapi.standard.ws.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import com.krstudy.kapi.com.krstudy.kapi.standard.ws.handler.TradeWebSocketHandler
@Configuration
@EnableWebSocket
class TradeWebSocketConfig(
    private val tradeWebSocketHandler: TradeWebSocketHandler
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(tradeWebSocketHandler, "/ws/trade")
            .setAllowedOrigins("*")
    }
}