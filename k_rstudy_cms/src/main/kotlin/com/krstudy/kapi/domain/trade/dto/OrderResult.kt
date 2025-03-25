package com.krstudy.kapi.domain.trade.dto

data class OrderResult(
    val orderId: String,
    val status: OrderStatus
)