package com.krstudy.kapi.standard.ws.registry

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Component
class WebSocketSessionRegistry {
    private val sessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()

    fun addSubscriber(coinCode: String, session: WebSocketSession) {
        sessions.computeIfAbsent(coinCode) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    fun removeSubscriber(coinCode: String, session: WebSocketSession) {
        sessions[coinCode]?.remove(session)
    }

    fun getSubscribers(coinCode: String): Set<WebSocketSession> {
        return sessions[coinCode] ?: emptySet()
    }

    fun removeSession(session: WebSocketSession) {
        sessions.values.forEach { it.remove(session) }
    }
}