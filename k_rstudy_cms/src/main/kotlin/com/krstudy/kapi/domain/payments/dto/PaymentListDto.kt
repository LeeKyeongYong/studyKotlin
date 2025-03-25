package com.krstudy.kapi.domain.payments.dto

import com.krstudy.kapi.domain.payments.status.PaymentStatus
import java.time.LocalDateTime

data class PaymentListDto(
    val id: Long,
    val orderId: String,
    val amount: Int,
    val status: PaymentStatus,
    val memberName: String,
    val memberId: String,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val canceledAt: LocalDateTime?,
    val receiptUrl: String,
    val paymentKey: String // 추가
)