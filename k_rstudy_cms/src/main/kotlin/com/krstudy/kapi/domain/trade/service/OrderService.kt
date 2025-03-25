package com.krstudy.kapi.domain.trade.service

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.trade.constant.OrderType
import com.krstudy.kapi.domain.trade.dto.OrderRequest
import com.krstudy.kapi.domain.trade.dto.OrderResult
import com.krstudy.kapi.domain.trade.dto.OrderStatus
import com.krstudy.kapi.domain.trade.entity.CoinOrder
import com.krstudy.kapi.domain.trade.event.OrderEvent
import com.krstudy.kapi.domain.trade.repository.OrderRepository
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.TimeUnit
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val balanceService: BalanceService,
    private val kafkaTemplate: KafkaTemplate<String, OrderEvent>,
    private val redisTemplate: RedisTemplate<String, String>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val lockTimeout = Duration.ofSeconds(30)

    @Transactional
    suspend fun processBuyOrder(order: OrderRequest, member: Member): OrderResult =
        withContext(Dispatchers.IO) {
            val lockKey = "order:${order.coinCode}:${member.userid}"

            try {
                // 분산 락 획득
                if (!acquireLock(lockKey)) {
                    throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
                }

                // 잔고 검증
                balanceService.validateAndLockBalance(
                    userId = member.userid,
                    amount = order.price.multiply(order.quantity)
                )

                // 주문 생성 및 저장
                val savedOrder = createAndSaveOrder(order, member, OrderType.BUY)

                // 주문 이벤트 발행
                publishOrderEvent(savedOrder)

                OrderResult(savedOrder.id.toString(), OrderStatus.PENDING)
            } catch (e: Exception) {
                logger.error("Buy order processing failed", e)
                balanceService.unlockBalance(member.userid)
                throw when (e) {
                    is GlobalException -> e
                    else -> GlobalException(MessageCode.PAYMENT_FAILED)
                }
            } finally {
                releaseLock(lockKey)
            }
        }

    private suspend fun acquireLock(lockKey: String): Boolean =
        withContext(Dispatchers.IO) {
            redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", lockTimeout) ?: false
        }

    private suspend fun releaseLock(lockKey: String) =
        withContext(Dispatchers.IO) {
            redisTemplate.delete(lockKey)
        }

    private suspend fun createAndSaveOrder(
        order: OrderRequest,
        member: Member,
        type: OrderType
    ): CoinOrder = withContext(Dispatchers.IO) {
        orderRepository.save(
            CoinOrder(
                userId = member.userid,
                coinCode = order.coinCode,
                type = type,
                price = order.price,
                quantity = order.quantity,
                status = OrderStatus.PENDING
            )
        )
    }

    private suspend fun publishOrderEvent(order: CoinOrder) =
        withContext(Dispatchers.IO) {
            try {
                kafkaTemplate.send(
                    "order-events",
                    OrderEvent(
                        orderId = order.id!!,
                        type = order.type,
                        userId = order.userId,
                        coinCode = order.coinCode,
                        price = order.price,
                        quantity = order.quantity
                    )
                ).get(5, TimeUnit.SECONDS) // 5초 타임아웃
            } catch (e: Exception) {
                logger.error("Failed to publish order event", e)
                throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
            }
        }

    @Transactional
    suspend fun processSellOrder(order: OrderRequest, member: Member): OrderResult =
        withContext(Dispatchers.IO) {
            val lockKey = "order:${order.coinCode}:${member.userid}"

            try {
                if (!acquireLock(lockKey)) {
                    throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
                }

                balanceService.validateAndLockCoinBalance(
                    userId = member.userid,
                    coinCode = order.coinCode,
                    amount = order.quantity
                )

                val savedOrder = createAndSaveOrder(order, member, OrderType.SELL)
                publishOrderEvent(savedOrder)

                OrderResult(savedOrder.id.toString(), OrderStatus.PENDING)
            } catch (e: Exception) {
                logger.error("Sell order processing failed", e)
                balanceService.unlockCoinBalance(member.userid, order.coinCode)
                throw when (e) {
                    is GlobalException -> e
                    else -> GlobalException(MessageCode.PAYMENT_FAILED)
                }
            } finally {
                releaseLock(lockKey)
            }
        }
}