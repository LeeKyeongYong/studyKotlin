package com.krstudy.kapi.domain.trade.client

import com.krstudy.kapi.com.krstudy.kapi.domain.trade.client.MarketDataClient
import com.krstudy.kapi.domain.trade.dto.HogaDto
import com.krstudy.kapi.domain.trade.dto.OrderBook
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Component
class MarketDataClientImpl(
    private val restTemplate: RestTemplate
) : MarketDataClient {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override suspend fun getHogaInfo(coinCode: String): HogaDto {
        try {
            // 실제 외부 API 호출 로직을 구현하세요
            // 여기서는 예시 데이터를 반환합니다
            return HogaDto(
                sellOrders = listOf(
                    OrderBook(BigDecimal("50000"), BigDecimal("1.5")),
                    OrderBook(BigDecimal("50100"), BigDecimal("2.0"))
                ),
                buyOrders = listOf(
                    OrderBook(BigDecimal("49900"), BigDecimal("1.0")),
                    OrderBook(BigDecimal("49800"), BigDecimal("2.5"))
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to fetch hoga info for $coinCode: ${e.message}", e)
            throw GlobalException(MessageCode.NOT_FOUND_RESOURCE)
        }
    }
}