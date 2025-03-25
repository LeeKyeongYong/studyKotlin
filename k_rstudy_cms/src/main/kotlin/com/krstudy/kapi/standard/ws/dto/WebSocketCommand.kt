package com.krstudy.kapi.standard.ws.dto

import com.krstudy.kapi.standard.ws.constant.CommandType

data class WebSocketCommand(
    val type: CommandType,
    val coinCode: String,
    val data: Any? = null
)