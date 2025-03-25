package com.krstudy.kapi.domain.trade.service

import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Duration

@Service
class BalanceService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    suspend fun validateAndLockBalance(userId: String, amount: BigDecimal) {
        val balanceKey = "balance:$userId"
        val lockKey = "balance_lock:$userId"

        // 잔고 검증
        val balance = getBalance(userId)
        if (balance < amount) {
            throw GlobalException(MessageCode.PAYMENT_AMOUNT_MISMATCH)
        }

        // 잔고 락
        val locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, amount.toString(), Duration.ofSeconds(30))

        if (locked != true) {
            throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
        }
    }

    suspend fun unlockBalance(userId: String) {
        val lockKey = "balance_lock:$userId"
        redisTemplate.delete(lockKey)
    }

    private fun getBalance(userId: String): BigDecimal {
        val balanceKey = "balance:$userId"
        return redisTemplate.opsForValue().get(balanceKey)?.toString()?.toBigDecimalOrNull()
            ?: BigDecimal.ZERO
    }

    suspend fun validateAndLockCoinBalance(userId: String, coinCode: String, amount: BigDecimal) {
        val balanceKey = "coin_balance:$userId:$coinCode"
        val lockKey = "coin_balance_lock:$userId:$coinCode"

        // 코인 잔고 검증
        val balance = getCoinBalance(userId, coinCode)
        if (balance < amount) {
            throw GlobalException(MessageCode.PAYMENT_AMOUNT_MISMATCH)
        }

        // 코인 잔고 락
        val locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, amount.toString(), Duration.ofSeconds(30))

        if (locked != true) {
            throw GlobalException(MessageCode.PAYMENT_PROCESSING_ERROR)
        }
    }

    suspend fun unlockCoinBalance(userId: String, coinCode: String) {
        val lockKey = "coin_balance_lock:$userId:$coinCode"
        redisTemplate.delete(lockKey)
    }

    private fun getCoinBalance(userId: String, coinCode: String): BigDecimal {
        val balanceKey = "coin_balance:$userId:$coinCode"
        return redisTemplate.opsForValue().get(balanceKey)?.toString()?.toBigDecimalOrNull()
            ?: BigDecimal.ZERO
    }

}