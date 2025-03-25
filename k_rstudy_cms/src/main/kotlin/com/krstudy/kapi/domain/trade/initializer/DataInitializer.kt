package com.krstudy.kapi.domain.trade.initializer

import com.krstudy.kapi.domain.trade.entity.Coin
import com.krstudy.kapi.domain.trade.repository.CoinRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Component
class DataInitializer(
    private val coinRepository: CoinRepository
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun run(vararg args: String) {
        try {
            if (coinRepository.count() == 0L) {
                val coins = listOf(
                    Coin.create(
                        code = "BTC",
                        name = "Bitcoin",
                        currentPrice = BigDecimal("50000000"),
                        changeRate = BigDecimal.ZERO,
                        volume24h = BigDecimal("1000000")
                    ),
                    Coin.create(
                        code = "ETH",
                        name = "Ethereum",
                        currentPrice = BigDecimal("3000000"),
                        changeRate = BigDecimal.ZERO,
                        volume24h = BigDecimal("500000")
                    ),
                    Coin.create(
                        code = "XRP",
                        name = "Ripple",
                        currentPrice = BigDecimal("500"),
                        changeRate = BigDecimal.ZERO,
                        volume24h = BigDecimal("100000")
                    )
                )

                coinRepository.saveAll(coins)
                logger.info("Initial coin data has been created")
            }
        } catch (e: Exception) {
            logger.error("Failed to initialize coin data", e)
        }
    }
}