package com.krstudy.kapi.domain.trade.dto

import java.math.BigDecimal

data class CoinDto(
    val code: String,
    val name: String,
    val currentPrice: BigDecimal,
    val changeRate: BigDecimal,
    val volume24h: BigDecimal
)