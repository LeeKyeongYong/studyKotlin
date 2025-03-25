package com.krstudy.kapi.domain.trade.entity

import com.krstudy.kapi.domain.trade.dto.CoinDto
import com.krstudy.kapi.global.jpa.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import org.hibernate.envers.Audited

@Entity
@Table(
    name = "coins",
    indexes = [
        Index(name = "uk_coins_code", columnList = "code", unique = true)
    ]
)
@Audited  // Envers 감사 기능 활성화
class Coin(

    @Column(name = "code", length = 20, nullable = false, unique = true)
    val code: String,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "current_price", nullable = false, precision = 19, scale = 8)
    var currentPrice: BigDecimal,

    @Column(name = "change_rate", nullable = false, precision = 19, scale = 8)
    var changeRate: BigDecimal,

    @Column(name = "volume_24h", nullable = false, precision = 19, scale = 8)
    var volume24h: BigDecimal,

    @Column(name = "active", nullable = false)
    var active: Boolean = true
) : BaseEntity() {

    fun toDto(): CoinDto = CoinDto(
        code = code,
        name = name,
        currentPrice = currentPrice,
        changeRate = changeRate,
        volume24h = volume24h
    )

    companion object {
        fun create(
            code: String,
            name: String,
            currentPrice: BigDecimal,
            changeRate: BigDecimal = BigDecimal.ZERO,
            volume24h: BigDecimal = BigDecimal.ZERO
        ): Coin {
            return Coin(
                code = code,
                name = name,
                currentPrice = currentPrice,
                changeRate = changeRate,
                volume24h = volume24h
            )
        }
    }
}