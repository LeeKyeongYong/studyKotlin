package com.krstudy.kapi.domain.trade.dto

import java.math.BigDecimal

data class OrderBook(
    val price: BigDecimal,
    val quantity: BigDecimal
)