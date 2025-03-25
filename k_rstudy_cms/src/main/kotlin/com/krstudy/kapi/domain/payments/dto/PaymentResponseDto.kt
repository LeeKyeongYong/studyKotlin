package com.krstudy.kapi.domain.payments.dto

import com.krstudy.kapi.domain.payments.status.PaymentStatus
import java.time.LocalDateTime

data class PaymentResponseDto(
    val orderId: String,
    val amount: Int,
    val status: PaymentStatus,
    val paymentKey: String,
    val completedAt: LocalDateTime?
)