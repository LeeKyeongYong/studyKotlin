package com.krstudy.kapi.domain.trade.repository

import com.krstudy.kapi.domain.trade.dto.OrderStatus
import com.krstudy.kapi.domain.trade.entity.CoinOrder
import com.krstudy.kapi.domain.trade.constant.OrderType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import jakarta.persistence.LockModeType
import java.math.BigDecimal
import java.time.Instant
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
interface OrderRepository : JpaRepository<CoinOrder, Long> {

    // 사용자의 특정 상태 주문 조회
    @Query("SELECT o FROM CoinOrder o WHERE o.userId = :userId AND o.status = :status")
    fun findByUserIdAndStatus(
        @Param("userId") userId: String,
        @Param("status") status: OrderStatus,
        pageable: Pageable
    ): Page<CoinOrder>

    // 주문 ID로 락을 걸고 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CoinOrder o WHERE o.id = :id")
    fun findByIdForUpdate(@Param("id") id: Long): CoinOrder?

    // 사용자의 특정 코인 주문 조회
    @Query("SELECT o FROM CoinOrder o WHERE o.userId = :userId AND o.coinCode = :coinCode")
    fun findByUserIdAndCoinCode(
        @Param("userId") userId: String,
        @Param("coinCode") coinCode: String,
        pageable: Pageable
    ): Page<CoinOrder>

    // 특정 코인의 미체결 매수 주문 조회
    @Query("""
        SELECT o FROM CoinOrder o 
        WHERE o.coinCode = :coinCode 
        AND o.type = 'BUY' 
        AND o.status = 'PENDING' 
        AND o.price >= :price 
        ORDER BY o.price DESC, o.createdAt ASC
    """)
    fun findPendingBuyOrders(
        @Param("coinCode") coinCode: String,
        @Param("price") price: BigDecimal,
        pageable: Pageable
    ): List<CoinOrder>

    // 특정 코인의 미체결 매도 주문 조회
    @Query("""
        SELECT o FROM CoinOrder o 
        WHERE o.coinCode = :coinCode 
        AND o.type = 'SELL' 
        AND o.status = 'PENDING' 
        AND o.price <= :price 
        ORDER BY o.price ASC, o.createdAt ASC
    """)
    fun findPendingSellOrders(
        @Param("coinCode") coinCode: String,
        @Param("price") price: BigDecimal,
        pageable: Pageable
    ): List<CoinOrder>

    // 특정 기간 동안의 거래량 조회
    @Query("""
        SELECT SUM(o.quantity) FROM CoinOrder o 
        WHERE o.coinCode = :coinCode 
        AND o.status = 'COMPLETED' 
        AND o.createdAt BETWEEN :startTime AND :endTime
    """)
    fun getTradeVolume(
        @Param("coinCode") coinCode: String,
        @Param("startTime") startTime: Instant,
        @Param("endTime") endTime: Instant
    ): BigDecimal?

    // 사용자의 미체결 주문 취소
    @Query("""
        UPDATE CoinOrder o 
        SET o.status = 'CANCELED' 
        WHERE o.userId = :userId 
        AND o.status = 'PENDING' 
        AND o.id = :orderId
    """)
    fun cancelOrder(
        @Param("userId") userId: String,
        @Param("orderId") orderId: Long
    ): Int

    // 특정 코인의 최근 체결가 조회
    @Query("""
        SELECT o.price FROM CoinOrder o 
        WHERE o.coinCode = :coinCode 
        AND o.status = 'COMPLETED' 
        ORDER BY o.createdAt DESC
    """)
    fun getLastTradePrice(
        @Param("coinCode") coinCode: String,
        pageable: Pageable
    ): List<BigDecimal>  // BigDecimal -> List<BigDecimal>로 변경

    @Query("""
        SELECT o.price FROM CoinOrder o 
        WHERE o.coinCode = :coinCode 
        AND o.status = 'COMPLETED' 
        ORDER BY o.createdAt DESC
        LIMIT 1
    """)
    fun getLastTradePriceOne(@Param("coinCode") coinCode: String): BigDecimal?
}