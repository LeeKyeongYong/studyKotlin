package com.krstudy.kapi.domain.trade.constant

enum class OrderType {
    BUY, SELL;

    companion object {
        fun fromString(value: String): OrderType {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid order type: $value. Allowed values are: ${values().joinToString()}")
            }
        }

        fun isBuy(type: OrderType): Boolean = type == BUY
        fun isSell(type: OrderType): Boolean = type == SELL
    }

    fun getDescription(): String {
        return when (this) {
            BUY -> "매수"
            SELL -> "매도"
        }
    }
}