package com.krstudy.kapi.domain.payments.repository

import com.krstudy.kapi.domain.member.entity.Member
import com.krstudy.kapi.domain.payments.entity.Payment
import com.krstudy.kapi.domain.payments.status.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByOrderId(orderId: String): Payment?
    fun findByMemberId(memberId: Long): List<Payment>
    fun findByPaymentKey(paymentKey: String): Payment?
    @Query("""
    SELECT p FROM Payment p
    LEFT JOIN p.member m
    WHERE (:memberId IS NULL OR m.id = :memberId)
    AND (:startDate IS NULL OR p.createdAt >= :startDate)
    AND (:endDate IS NULL OR p.createdAt <= :endDate)
    AND (:status IS NULL OR p.status = :status)
    AND (:memberName IS NULL OR m.username LIKE %:memberName%)
    AND (:orderId IS NULL OR p.orderId LIKE %:orderId%)
""")
    fun searchPayments(
        memberId: Long?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?,
        status: PaymentStatus?,
        memberName: String?,
        orderId: String?,
        pageable: Pageable
    ): Page<Payment>
}