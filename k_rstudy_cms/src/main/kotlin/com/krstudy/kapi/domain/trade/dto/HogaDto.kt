package com.krstudy.kapi.domain.trade.dto

data class HogaDto(
    val sellOrders: List<OrderBook>,
    val buyOrders: List<OrderBook>
)