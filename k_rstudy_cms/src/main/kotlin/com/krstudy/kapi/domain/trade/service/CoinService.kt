package com.krstudy.kapi.domain.trade.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.krstudy.kapi.com.krstudy.kapi.domain.trade.client.MarketDataClient
import com.krstudy.kapi.domain.trade.dto.CoinDto
import com.krstudy.kapi.domain.trade.dto.HogaDto
import com.krstudy.kapi.domain.trade.entity.Coin
import com.krstudy.kapi.domain.trade.repository.CoinRepository
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.math.BigDecimal

@Service
class CoinService(
    private val coinRepository: CoinRepository,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val marketDataClient: MarketDataClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper = jacksonObjectMapper()

    // Flow를 반환하는 메서드 (DTO 반환)
    @Transactional(readOnly = true)
    suspend fun getAllCoinsAsFlow(): Flow<CoinDto> = flow {
        try {
            coinRepository.findAllByOrderByCodeAsc()
                .forEach { coin ->
                    emit(coin.toDto())
                }
        } catch (e: Exception) {
            logger.error("Error fetching all coins", e)
            throw GlobalException(MessageCode.NOT_FOUND_RESOURCE)
        }
    }

    // List를 반환하는 메서드 (Entity 반환)
    @Transactional(readOnly = true)
    suspend fun getAllCoins(): List<Coin> = withContext(Dispatchers.IO) {
        try {
            coinRepository.findAll()
        } catch (e: Exception) {
            logger.error("Error fetching all coins", e)
            throw GlobalException(MessageCode.SYSTEM_ERROR)
        }
    }

    suspend fun getHogaInfo(coinCode: String): HogaDto = withContext(Dispatchers.IO) {
        val cacheKey = "hoga:$coinCode"

        try {
            // 캐시에서 먼저 조회
            redisTemplate.opsForValue().get(cacheKey)?.let {
                return@withContext objectMapper.readValue(it.toString(), HogaDto::class.java)
            }

            // 캐시에 없으면 외부 API 호출
            val hogaInfo = marketDataClient.getHogaInfo(coinCode)

            // 캐시에 저장
            redisTemplate.opsForValue().set(
                cacheKey,
                objectMapper.writeValueAsString(hogaInfo),
                Duration.ofSeconds(1)
            )

            hogaInfo
        } catch (e: Exception) {
            logger.error("Error fetching hoga info for coin: $coinCode", e)
            throw GlobalException(MessageCode.NOT_FOUND_RESOURCE)
        }
    }

    @Transactional(readOnly = true)
    suspend fun getCoinByCode(code: String): Coin = withContext(Dispatchers.IO) {
        try {
            // findById 대신 findByCode 사용
            coinRepository.findByCode(code)?.let { coin ->
                return@withContext coin
            } ?: throw GlobalException(MessageCode.NOT_FOUND_RESOURCE)
        } catch (e: Exception) {
            logger.error("Error fetching coin by code: $code", e)
            when (e) {
                is GlobalException -> throw e
                else -> throw GlobalException(MessageCode.SYSTEM_ERROR)
            }
        }
    }

    // 코인 정보 업데이트
    @Transactional
    suspend fun updateCoinPrice(code: String, newPrice: BigDecimal) = withContext(Dispatchers.IO) {
        try {
            val coin = getCoinByCode(code)
            coin.currentPrice = newPrice
            coinRepository.save(coin)
        } catch (e: Exception) {
            logger.error("Error updating coin price: $code", e)
            throw GlobalException(MessageCode.SYSTEM_ERROR)
        }
    }
}