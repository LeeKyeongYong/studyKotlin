package com.krstudy.kapi.domain.payments.dto

import java.math.BigDecimal

data class PaymentRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long
)