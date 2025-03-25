package com.krstudy.kapi.domain.trade.event

import com.krstudy.kapi.domain.trade.constant.OrderType
import java.math.BigDecimal

data class OrderEvent(
    val orderId: Long,
    val type: OrderType,
    val userId: String,
    val coinCode: String,
    val price: BigDecimal,
    val quantity: BigDecimal,
    val timestamp: Long = System.currentTimeMillis()
)