package com.krstudy.kapi.domain.trade.controller

import com.krstudy.kapi.domain.trade.dto.OrderForm
import com.krstudy.kapi.domain.trade.service.CoinService
import com.krstudy.kapi.global.https.ReqData
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import com.krstudy.kapi.global.exception.GlobalException
import com.krstudy.kapi.global.exception.MessageCode
import org.springframework.data.redis.core.RedisTemplate
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestMapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Controller
@RequestMapping("/trade")
class CoinController(
    private val rq: ReqData,
    private val coinService: CoinService,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    init {
        logger.info("CoinController initialized")
    }

    @GetMapping("/service")
    suspend fun tradeService(): String {
        logger.info("거래 서비스 엔드포인트 접근")

        try {
            // 메인 스레드에서 request scope 빈 접근
            val member = rq.getMember() ?: throw GlobalException(MessageCode.UNAUTHORIZED)

            // IO 작업은 IO 디스패처에서 수행
            val (coin, hogaInfo) = withContext(Dispatchers.IO) {
                val coinCode = redisTemplate.opsForValue()
                    .get("coin:${member.userid}:code")?.toString() ?: "BTC"

                val coin = coinService.getCoinByCode(coinCode)
                val hogaInfo = coinService.getHogaInfo(coinCode)

                Pair(coin, hogaInfo)
            }

            // 다시 메인 스레드에서 request scope 작업 수행
            val buyOrder = OrderForm(
                coinName = coin.name,
                price = null,
                quantity = null,
                total = null
            )

            rq.setAttribute("member", member)
            rq.setAttribute("coin", coin)
            rq.setAttribute("hogaList", hogaInfo)
            rq.setAttribute("buyOrder", buyOrder)

            return "domain/dashboard/tradeService"

        } catch (e: Exception) {
            logger.error("거래 서비스 오류: ${e.message}", e)
            throw when (e) {
                is GlobalException -> e
                else -> GlobalException(MessageCode.SYSTEM_ERROR)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CoinController::class.java)
    }
}