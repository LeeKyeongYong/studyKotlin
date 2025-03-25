package com.krstudy.kapi.standard.ws.dto

data class WebSocketMessage(
    val type: String,
    val coinCode: String,
    val data: Any
)