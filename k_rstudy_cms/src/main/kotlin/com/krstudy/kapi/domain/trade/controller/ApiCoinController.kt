package com.krstudy.kapi.domain.trade.controller

import org.springframework.web.bind.annotation.*
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.krstudy.kapi.com.krstudy.kapi.standard.ws.handler.TradeWebSocketHandler
import java.time.Duration
import jakarta.validation.Valid
import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.trade.service.CoinService
import com.krstudy.kapi.domain.trade.service.OrderService
import com.krstudy.kapi.domain.trade.dto.*
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.data.redis.core.RedisCallback
import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

@RestController
@RequestMapping("/api/v1")
@Validated
class ApiCoinController(
    private val coinService: CoinService,
    private val orderService: OrderService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val webSocketHandler: TradeWebSocketHandler
) {
    // 코인 목록 조회 - Redis Cache 적용
    @GetMapping("/coins")
    suspend fun getCoinList(): Flow<CoinDto> =
        withContext(Dispatchers.IO) {
            val cachedCoins = redisTemplate.opsForValue().get("coins")?.toString()

            if (cachedCoins != null) {
                flow {
                    val coinsList = jacksonObjectMapper().readValue<List<CoinDto>>(
                        cachedCoins,
                        object : TypeReference<List<CoinDto>>() {}
                    )
                    coinsList.forEach { emit(it) }
                }
            } else {
                // getAllCoinsAsFlow() 메서드 사용
                coinService.getAllCoinsAsFlow().also { coinsFlow ->
                    // Flow를 List로 변환하여 캐시
                    val coinsList = coinsFlow.toList()
                    redisTemplate.opsForValue().set(
                        "coins",
                        jacksonObjectMapper().writeValueAsString(coinsList),
                        Duration.ofMinutes(5)
                    )
                }
            }
        }

    // 호가 정보 조회
    @GetMapping("/hoga/{coinCode}")
    suspend fun getHogaInfo(@PathVariable coinCode: String): HogaDto =
        withContext(Dispatchers.IO) {
            coinService.getHogaInfo(coinCode)
                .also { hoga ->
                    webSocketHandler.sendHogaUpdate(coinCode, hoga)
                }
        }

    // 매수 주문 처리
    @PostMapping("/orders/buy")
    suspend fun buyOrder(
        @Valid @RequestBody order: OrderRequest,
        @AuthenticationPrincipal member: Member
    ): OrderResult = withContext(Dispatchers.IO) {
        val lockKey = "order:${order.coinCode}:${member.userid}"

        redisTemplate.execute(RedisCallback { connection ->
            if (connection.setNX(lockKey.toByteArray(), ByteArray(0))) {
                connection.expire(lockKey.toByteArray(), 30) // 30초 타임아웃
                true
            } else {
                false
            }
        })?.let { locked ->
            if (locked) {
                try {
                    orderService.processBuyOrder(order, member)
                } finally {
                    redisTemplate.delete(lockKey)
                }
            } else {
                throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
            }
        } ?: throw GlobalException(MessageCode.PAYMENT_FAILED)
    }

    // 매도 주문 처리
    @PostMapping("/orders/sell")
    suspend fun sellOrder(
        @Valid @RequestBody order: OrderRequest,
        @AuthenticationPrincipal member: Member
    ): OrderResult = withContext(Dispatchers.IO) {
        val lockKey = "order:${order.coinCode}:${member.userid}"

        redisTemplate.execute(RedisCallback { connection ->
            if (connection.setNX(lockKey.toByteArray(), ByteArray(0))) {
                connection.expire(lockKey.toByteArray(), 30)
                true
            } else {
                false
            }
        })?.let { locked ->
            if (locked) {
                try {
                    orderService.processSellOrder(order, member)
                } finally {
                    redisTemplate.delete(lockKey)
                }
            } else {
                throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
            }
        } ?: throw GlobalException(MessageCode.PAYMENT_FAILED)
    }
}