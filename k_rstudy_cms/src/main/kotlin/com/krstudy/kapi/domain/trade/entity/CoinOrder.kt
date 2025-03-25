package com.krstudy.kapi.domain.trade.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import com.krstudy.kapi.domain.trade.dto.OrderStatus
import com.krstudy.kapi.domain.trade.constant.OrderType
import com.krstudy.kapi.global.jpa.BaseEntity

@Entity
@Table(name = "coin_orders")
class CoinOrder(

    @Column(nullable = false)
    val userId: String,

    @Column(nullable = false)
    val coinCode: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: OrderType,

    @Column(nullable = false, precision = 19, scale = 8)
    val price: BigDecimal,

    @Column(nullable = false, precision = 19, scale = 8)
    val quantity: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
) : BaseEntity() {

    // 주문 총액 계산
    fun getTotalAmount(): BigDecimal = price.multiply(quantity)

    // 주문 상태 검증
    fun isProcessable(): Boolean = status == OrderStatus.PENDING

    // 주문 타입 검증
    fun isBuyOrder(): Boolean = OrderType.isBuy(type)
    fun isSellOrder(): Boolean = OrderType.isSell(type)

    companion object {
        fun createBuyOrder(userId: String, coinCode: String, price: BigDecimal, quantity: BigDecimal): CoinOrder {
            return CoinOrder(
                userId = userId,
                coinCode = coinCode,
                type = OrderType.BUY,
                price = price,
                quantity = quantity,
                status = OrderStatus.PENDING
            )
        }

        fun createSellOrder(userId: String, coinCode: String, price: BigDecimal, quantity: BigDecimal): CoinOrder {
            return CoinOrder(
                userId = userId,
                coinCode = coinCode,
                type = OrderType.SELL,
                price = price,
                quantity = quantity,
                status = OrderStatus.PENDING
            )
        }
    }
}