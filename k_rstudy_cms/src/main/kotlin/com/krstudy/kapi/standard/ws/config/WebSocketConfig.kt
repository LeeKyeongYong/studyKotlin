package com.krstudy.kapi.com.krstudy.kapi.standard.ws.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue", "/user") // /user 추가
        config.setApplicationDestinationPrefixes("/app")
        config.setUserDestinationPrefix("/user") // 개인화된 메시지를 위한 prefix 추가
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws/trade", "/ws/monitoring")  // 두 엔드포인트를 함께 등록
            .setAllowedOriginPatterns("*")
            .withSockJS()
            .setStreamBytesLimit(512 * 1024)
            .setHttpMessageCacheSize(1000)
            .setDisconnectDelay(30 * 1000)
    }



    override fun configureWebSocketTransport(registration: WebSocketTransportRegistration) {
        registration
            .setMessageSizeLimit(128 * 1024)     // 메시지 크기 제한 128KB
            .setSendBufferSizeLimit(512 * 1024)  // 버퍼 크기 제한 512KB
            .setSendTimeLimit(20 * 1000)         // 전송 시간 제한 20초
    }
}