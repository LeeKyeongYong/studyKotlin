package com.krstudy.kapi.domain.payments.dto

import com.krstudy.kapi.domain.payments.status.PaymentStatus

data class PaymentResponse(
    val orderId: String,
    val status: PaymentStatus,
    val message: String
)