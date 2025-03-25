package com.krstudy.kapi.domain.payments.dto

import com.krstudy.kapi.domain.payments.status.PaymentStatus
import java.time.LocalDateTime

data class PaymentCancelResponseDto(
    val paymentKey: String,
    val orderId: String,
    val status: PaymentStatus,
    val transactionKey: String?,
    val cancelReason: String,
    val canceledAt: LocalDateTime,
    val cancelAmount: Int,
    val remainingAmount: Int
)